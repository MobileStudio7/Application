package com.example.my_camera2

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CaptureRequest.*
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.NonNull
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var takePictureButton: Button? = null
    private var textureView: TextureView? = null
    private var cameraId : String? = null
    private var cameraDevice: CameraDevice? = null
    private lateinit var cameraCaptureSessions: CameraCaptureSession
    private var captureRequest: CaptureRequest? = null
    private lateinit var captureRequestBuilder: Builder
    private var imageDimension: Size? = null
    private var imageReader: ImageReader? = null
    private val file: File? = null
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    private var btChoose : Button? = null
    private var btUpload : Button? = null
    private var ivPreview : ImageView? = null
    private var filePath : Uri? = null
    private var sensorOrientation = 0
    private val PICK_IMAGE_MULTIPLE = 1

    private var textureListener: TextureView.SurfaceTextureListener =
        object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                //open your camera here
                openCamera()
            }
            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            )
            {
                // Transform you image captured size according to the surface width and height

            }
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }

    private val stateCallback =  object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened")
            cameraDevice = camera
            createCameraPreview()
        }
        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice!!.close()
        }
        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice!!.close()
            this@MainActivity.finish()
        }
    }
    private val STATE_PREVIEW = 0
    private val STATE_WAITING_LOCK = 1
    private val STATE_WAITING_PRECAPTURE = 2
    private val STATE_WAITING_NON_PRECAPTURE = 3
    private val STATE_PICTURE_TAKEN = 4
    private var state = STATE_PREVIEW
    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        private fun process(result: CaptureResult) {
            when (state) {
                STATE_PREVIEW -> Unit // Do nothing when the camera preview is working normally.
                STATE_WAITING_LOCK -> capturePicture(result)
                STATE_WAITING_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CONTROL_AE_STATE_FLASH_REQUIRED
                    ) {
                        state = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        private fun capturePicture(result: CaptureResult) {
            val afState = result.get(CaptureResult.CONTROL_AF_STATE)
            if (afState == null) {
                captureStillPicture()
            } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                // CONTROL_AE_STATE can be null on some devices
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    state = STATE_PICTURE_TAKEN
                    captureStillPicture()
                } else {
                    runPrecaptureSequence()
                }
            }
        }

        override fun onCaptureProgressed(session: CameraCaptureSession,
                                         request: CaptureRequest,
                                         partialResult: CaptureResult) {
            process(partialResult)
        }

        override fun onCaptureCompleted(session: CameraCaptureSession,
                                        request: CaptureRequest,
                                        result: TotalCaptureResult) {
              process(result)
             }
        }
    private fun runPrecaptureSequence() {
            try {
                // This is how to tell the camera to trigger.
                captureRequestBuilder.set(
                    CONTROL_AE_PRECAPTURE_TRIGGER,
                    CONTROL_AE_PRECAPTURE_TRIGGER_START
                )
                // Tell #captureCallback to wait for the precapture sequence to be set.
                state = STATE_WAITING_PRECAPTURE
                cameraCaptureSessions?.capture(captureRequestBuilder.build(), captureCallback,
                    mBackgroundHandler)
            } catch (e: CameraAccessException) {
                Log.e(TAG, e.toString())
            }
        }


    private fun captureStillPicture() {
        try {
            if (cameraDevice == null) return
            val rotation = this@MainActivity.windowManager.defaultDisplay.rotation

            // This is the CaptureRequest.Builder that we use to take a picture.
            val captureBuilder = cameraDevice?.createCaptureRequest(
                CameraDevice.TEMPLATE_STILL_CAPTURE)?.apply {
                addTarget(imageReader!!.surface)

                // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
                // We have to take that into account and rotate JPEG properly.
                // For devices with orientation of 90, we return our mapping from ORIENTATIONS.
                // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
                set(
                    JPEG_ORIENTATION,
                    (ORIENTATIONS.get(rotation) + sensorOrientation + 270) % 360)

                // Use the same AE and AF modes as the preview.
                set(
                    CONTROL_AF_MODE,
                    CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
            }?.also {  }

            val captureCallback = object : CameraCaptureSession.CaptureCallback() {

                override fun onCaptureCompleted(session: CameraCaptureSession,
                                                request: CaptureRequest,
                                                result: TotalCaptureResult) {
                    Log.d(TAG, file.toString())
                    unlockFocus()
                }
            }

            cameraCaptureSessions?.apply {
                stopRepeating()
                abortCaptures()
                capture(captureBuilder!!.build(), captureCallback, null)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun unlockFocus() {
        try {
            // Reset the auto-focus trigger
            captureRequestBuilder.set(
                CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            cameraCaptureSessions?.capture(captureRequestBuilder.build(), captureCallback,
                mBackgroundHandler)
            // After this, the camera will go back to the normal state of preview.
            state = STATE_PREVIEW
            cameraCaptureSessions?.setRepeatingRequest(captureRequest!!, captureCallback,
                mBackgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btChoose = findViewById(R.id.bt_choose)
        btUpload = findViewById(R.id.bt_upload)
        ivPreview = findViewById(R.id.iv_preview)

        btChoose!!.setOnClickListener {
            //이미지를 선택
            val intent = Intent()
            intent.type = "image/"
            intent.action = ACTION_GET_CONTENT
            startActivityForResult(intent,1)
        }
        btUpload!!.setOnClickListener {
            //업로드
            uploadFile()
        }

        // 카메라 뷰 설정
        textureView = findViewById(R.id.texture)
        assert(textureView != null)
        textureView!!.surfaceTextureListener = textureListener
        takePictureButton = findViewById(R.id.btn_takepicture)
        assert(takePictureButton != null)
        takePictureButton!!.setOnClickListener { takePicture() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //request코드가 1이고 OK를 선택했고 data에 뭔가가 들어 있다면
        if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && data != null) {
            this.filePath = data.data
            Log.d(TAG, "uri:$String")
            try {
                //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                ivPreview!!.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    //upload the file
    private fun uploadFile() {
        //업로드할 파일이 있으면 수행
        if (filePath != null) {
            //업로드 진행 Dialog 보이기
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("업로드중...")
            progressDialog.show()

            //storage
            val storage = FirebaseStorage.getInstance()

            //Unique한 파일명을 만들자.
            val formatter = SimpleDateFormat("yyyyMMHH_mmss")
            val now = Date()
            val filename = formatter.format(now) + ".png"
            //storage 주소와 폴더 파일명을 지정해 준다.
            val storageRef = storage.getReferenceFromUrl("gs://mycamera2-81bb3.appspot.com")
                .child("images/$filename")
            //올라가거라...
            storageRef.putFile(filePath!!)
                //성공시
                .addOnSuccessListener {
                    progressDialog.dismiss() //업로드 진행 Dialog 상자 닫기
                    iv_preview.setImageBitmap(null)
                    Toast.makeText(applicationContext, "업로드 완료!", Toast.LENGTH_SHORT)
                        .show()
                }
                //실패시
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "업로드 실패!", Toast.LENGTH_SHORT)
                        .show()
                }
                //진행중
                .addOnProgressListener { taskSnapshot ->
                    val progress =
                        100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount//이걸 넣어 줘야 아랫줄에 에러가 사라진다. 넌 누구냐?
                    //dialog에 진행률을 퍼센트로 출력해 준다
                    progressDialog.setMessage("Uploaded " + progress.toInt() + "% ...")
                }
        } else {
            Toast.makeText(applicationContext, "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {
        mBackgroundHandler?.post(ImageSaver(it.acquireNextImage(), file!!))
    }


    private fun takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null")
            return
        }
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val characteristics = manager.getCameraCharacteristics(cameraDevice!!.id)

                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (cameraDirection != null &&
                    cameraDirection == CameraCharacteristics.LENS_FACING_FRONT){}

                val map =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(
                        ImageFormat.JPEG
                    )
                var width = 640
                var height = 480
                if (map != null && map.isNotEmpty()) {
                    width = map[0].width
                    height = map[0].height
                }

                imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG,2).apply {
                    setOnImageAvailableListener(onImageAvailableListener,mBackgroundHandler)
                }
                val outputSurfaces = ArrayList<Surface>(2)
                outputSurfaces.add(imageReader!!.surface)
                outputSurfaces.add(Surface(textureView!!.surfaceTexture))
                val captureBuilder =
                    cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                captureBuilder.addTarget(imageReader!!.surface)
                captureBuilder.set(CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

                // Orientation
                val rotation = windowManager.defaultDisplay.rotation
                captureBuilder.set(JPEG_ORIENTATION, ORIENTATIONS.get(rotation))
                val file = File(Environment.getExternalStorageDirectory().toString() + "/pic.jpg")
                val readerListener = object : ImageReader.OnImageAvailableListener {
                    override fun onImageAvailable(reader: ImageReader) {
                        var image: Image? = null
                        try {
                            image = reader.acquireLatestImage()
                            val buffer = image!!.planes[0].buffer
                            val bytes = ByteArray(buffer.capacity())
                            buffer.get(bytes)
                            save(bytes)
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        finally {
                        image?.close()
                        }
                    }

                    @Throws(IOException::class)
                    private fun save(bytes: ByteArray) {
                        var output: OutputStream? = null
                        try {
                            output = FileOutputStream(file)
                            output.write(bytes)
                        } finally {
                            output?.close()
                        }
                    }
                }
                imageReader!!.setOnImageAvailableListener(readerListener, mBackgroundHandler)
                val captureListener = object : CameraCaptureSession.CaptureCallback() {
                    override fun onCaptureCompleted(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        result: TotalCaptureResult
                    ) {
                        super.onCaptureCompleted(session, request, result)
                        Toast.makeText(this@MainActivity, "Saved:$file", Toast.LENGTH_SHORT).show()
                        createCameraPreview()
                    }
                }
                cameraDevice!!.createCaptureSession(
                    outputSurfaces,
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            try {
                                session.capture(
                                    captureBuilder.build(),
                                    captureListener,
                                    mBackgroundHandler
                                )
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }

                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {}
                    },
                    mBackgroundHandler
                )
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

    }

    private fun createCameraPreview() {
        try {
            val texture = textureView!!.surfaceTexture
            texture!!.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)
            cameraDevice!!.createCaptureSession(Arrays.asList(surface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession : CameraCaptureSession) {
                        //The camera is already closed
                        if (null == cameraDevice) { return }
                        // When the session is ready, we start displaying the preview.
                        cameraCaptureSessions = cameraCaptureSession
                        updatePreview()
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(
                            this@MainActivity,
                            "Configuration change",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, null)
            } catch (e: CameraAccessException) {
                Log.e(TAG, e.toString())
        }

    }

    private fun openCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.e(TAG, "is camera open")
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (cameraDirection != null &&
                    cameraDirection == CameraCharacteristics.LENS_FACING_FRONT
                ) {
                    continue
                }


                val map =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
                // Add permission for camera and let user grant the permission
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) !== PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ),
                        REQUEST_CAMERA_PERMISSION
                    )
                    return
                }
                manager.openCamera(cameraId!!, stateCallback, mBackgroundHandler)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
        catch (e: InterruptedException){
            throw RuntimeException("Interrupted while trying lock camera opening")
        }
        Log.e(TAG, "openCamera X")
    }

    private fun updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return")
        }
        try {
            captureRequestBuilder.set(CONTROL_AF_MODE, CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            captureRequest = captureRequestBuilder.build()
            cameraCaptureSessions?.setRepeatingRequest(
                captureRequest!!,
                captureCallback,
                mBackgroundHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun closeCamera() {
        if (null != cameraDevice) {
            cameraDevice!!.close()
            cameraDevice = null
        }
        if (null != imageReader) {
            imageReader!!.close()
            imageReader = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(this@MainActivity, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
        startBackgroundThread()
        if (textureView!!.isAvailable) {
            openCamera()
        } else {
            textureView!!.surfaceTextureListener = textureListener
        }
    }

    override fun onPause() {
        Log.e(TAG, "onPause")
        super.onPause()
        closeCamera()
        stopBackgroundThread()
    }


    companion object {
        private val TAG = "MainActivity"
        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        private val REQUEST_CAMERA_PERMISSION = 200
    }
}

internal class ImageSaver(

    private val image: Image,
    private val file: File
) : Runnable {

    override fun run() {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        var output: FileOutputStream? = null
        try {
            output = FileOutputStream(file).apply {
                write(bytes)
            }
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        } finally {
            image.close()
            output?.let {
                try {
                    it.close()
                } catch (e: IOException) {
                    Log.e(TAG, e.toString())
                }
            }
        }
    }
    companion object {
        private val TAG = "ImageSaver"
    }
}
