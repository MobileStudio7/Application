package com.example.my_camera2

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
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
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var takePictureButton: Button? = null
    private var textureView: TextureView? = null
    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private lateinit var cameraCaptureSessions: CameraCaptureSession
    private var captureRequest: CaptureRequest? = null
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private var imageDimension: Size? = null
    private var imageReader: ImageReader? = null
    private val file: File? = null
    private val mFlashSupported: Boolean = false
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    private var btChoose : Button? = null
    private var btUpload : Button? = null
    private var ivPreview : ImageView? = null
    private var filePath : Uri? = null
    private var mStorageRef : StorageReference? = null

    private var textureListener: TextureView.SurfaceTextureListener =
        object : TextureView.SurfaceTextureListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
            ) {
                // Transform you image captured size according to the surface width and height
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    private val stateCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : CameraDevice.StateCallback() {
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
            cameraDevice = null
        }
    }
    protected val captureCallbackListener: CameraCaptureSession.CaptureCallback =
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
                Toast.makeText(this@MainActivity, "Saved:" + file!!, Toast.LENGTH_SHORT).show()
                createCameraPreview()
            }
        }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // DB연동 부분
        btChoose = findViewById(R.id.bt_choose)
        btUpload = findViewById(R.id.bt_upload)
        ivPreview = findViewById(R.id.iv_preview)

        btChoose!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                //이미지를 선택
                val intent = Intent()
                intent.setType("image/*")
                intent.setAction(Intent.ACTION_GET_CONTENT)
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 0)
            }
        })
        btUpload!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                //업로드
                uploadFile()
            }
        })

        // 카메라 뷰 설정
        textureView = findViewById(R.id.texture) as TextureView
        assert(textureView != null)
        textureView!!.surfaceTextureListener = textureListener
        takePictureButton = findViewById(R.id.btn_takepicture) as Button
        assert(takePictureButton != null)
        takePictureButton!!.setOnClickListener { takePicture() }
    }

    //결과 처리
    internal fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        //request코드가 0이고 OK를 선택했고 data에 뭔가가 들어 있다면
        if (requestCode == 0 && resultCode == RESULT_OK) {
            filePath = data.getData()
            Log.d(TAG, "uri:$String")
            try {
                //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
                val bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath)
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
            val storageRef = storage.getReferenceFromUrl("gs://yourStorage.appspot.com")
                .child("images/$filename")
            //올라가거라...
            storageRef.putFile(filePath!!)
                //성공시
                .addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
                    override fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot) {
                        progressDialog.dismiss() //업로드 진행 Dialog 상자 닫기
                        Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
                //실패시
                .addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(@NonNull e: Exception) {
                        progressDialog.dismiss()
                        Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
                //진행중
                .addOnProgressListener(object : OnProgressListener<UploadTask.TaskSnapshot> {
                    override fun onProgress(taskSnapshot: UploadTask.TaskSnapshot) {
                        val progress =
                            100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()//이걸 넣어 줘야 아랫줄에 에러가 사라진다. 넌 누구냐?
                        //dialog에 진행률을 퍼센트로 출력해 준다
                        progressDialog.setMessage("Uploaded " + progress.toInt() + "% ...")
                    }
                })
        } else {
            Toast.makeText(getApplicationContext(), "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    protected fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected fun takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null")
            return
        }
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val characteristics = manager.getCameraCharacteristics(cameraDevice!!.id)
                var jpegSizes: Array<Size>? = null
                if (characteristics != null) {
                    jpegSizes =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(
                            ImageFormat.JPEG
                        )
                }
                var width = 640
                var height = 480
                if (jpegSizes != null && 0 < jpegSizes.size) {
                    width = jpegSizes[0].width
                    height = jpegSizes[0].height
                }
                val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
                val outputSurfaces = ArrayList<Surface>(2)
                outputSurfaces.add(reader.surface)
                outputSurfaces.add(Surface(textureView!!.surfaceTexture))
                val captureBuilder =
                    cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                captureBuilder.addTarget(reader.surface)
                captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                // Orientation
                val rotation = getWindowManager().getDefaultDisplay().getRotation()
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))
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
                        } finally {
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
                reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected fun createCameraPreview() {
        try {
            val texture = textureView!!.surfaceTexture!!
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)
            cameraDevice!!.createCaptureSession(
                Arrays.asList(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(@NonNull cameraCaptureSession: CameraCaptureSession) {
                        //The camera is already closed
                        if (null == cameraDevice) {
                            return
                        }
                        // When the session is ready, we start displaying the preview.
                        cameraCaptureSessions = cameraCaptureSession
                        updatePreview()
                    }

                    override fun onConfigureFailed(@NonNull cameraCaptureSession: CameraCaptureSession) {
                        Toast.makeText(
                            this@MainActivity,
                            "Configuration change",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun openCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        Log.e(TAG, "is camera open")
        try {
            cameraId = manager.cameraIdList[0]
            val characteristics = manager.getCameraCharacteristics(cameraId!!)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
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
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CAMERA_PERMISSION
                )
                return
            }
            manager.openCamera(cameraId!!, stateCallback, null)
        } catch (@SuppressLint("NewApi") e: CameraAccessException) {
            e.printStackTrace()
        }

        Log.e(TAG, "openCamera X")
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected fun updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return")
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            cameraCaptureSessions.setRepeatingRequest(
                captureRequestBuilder.build(),
                null,
                mBackgroundHandler
            )
        } catch (@SuppressLint("NewApi") e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(this@MainActivity, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    protected override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume")
        startBackgroundThread()
        if (textureView!!.isAvailable) {
            openCamera()
        } else {
            textureView!!.surfaceTextureListener = textureListener
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected override fun onPause() {
        Log.e(TAG, "onPause")
        //closeCamera();
        stopBackgroundThread()
        super.onPause()
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