package com.example.projectresult.ui.home


import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.*
import android.util.Log
import android.util.SparseIntArray
import android.view.*
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.projectresult.R
import com.example.projectresult.ui.account.LoginFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*

class CameraFragment : Fragment() {

    // 어떤 capture의 요청을 받기위한 세션과 그 요청을 만들어 전달하는 builder
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder : CaptureRequest.Builder

    // 카메라가 열리면 미리보기를 띄우기 위한 연결세션
    private fun previewSession(){
        val surfaceTexture = textureView.surfaceTexture // layout의 view를 가져옴
        //surfaceTexture.setDefaultBufferSize()
        val surface = Surface(surfaceTexture) // Surface?
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW) // 카메라의 template_preview 타입의 요청을 빌더가 저장
        captureRequestBuilder.addTarget(surface) // 그 요청을 내가 만든 view랑 연결

        cameraDevice.createCaptureSession(Arrays.asList(surface),  // builder가 받아 둔 요청들을 내가 만든 surface(view)에 전달(statecallback과 함께 -> 현재 내가 사진을 찍는지 뭐하는지를 인식해야.
        object : CameraCaptureSession.StateCallback(){
            override fun onConfigured(session: CameraCaptureSession) {
                if(session != null) {
                    cameraCaptureSession = session
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                    cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),null,null)
                }
            }
            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.e(TAG, "Fail to create captureSesstion")
            }
        },null)
    }

    private fun closeCamera(){
        if(this::cameraCaptureSession.isInitialized)
            cameraCaptureSession.close()
        if(this::cameraDevice.isInitialized)
            cameraDevice.close()
    }


    // 기기가 실제로 존재하는지에 대한 여부와 그에 따른 state를 나타내는 callback함수
    private lateinit var cameraDevice: CameraDevice
    private val deviceStateCallBack = object : CameraDevice.StateCallback(){
        override fun onDisconnected(camera: CameraDevice) {
            Log.d("TAG", "camera device disconnected")
            camera.close()
        }
        override fun onError(camera: CameraDevice, error: Int) {
            Log.d("TAG", "camera device error")
            this@CameraFragment.activity?.finish()
        }
        override fun onOpened(camera: CameraDevice) {
            Log.d("TAG", "camera device open")
            if(camera != null) {
                cameraDevice = camera
                previewSession()
            }
        }
    }

    // thread와 thread를 다루기 위한 handler
    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroudHandler : Handler

    private fun startBackgroundThread(){
        backgroundThread = HandlerThread("Camera")
        backgroundThread.start()
        backgroudHandler = Handler(backgroundThread.looper)
    }

    private fun stopBackgroundThread(){
        backgroundThread.quitSafely()
        try{
            backgroundThread.join()
        }catch (e: InterruptedException) {
            Log.e(TAG, e.toString())
        }
    }
    // 카메라 매니저 세팅 -> 액티비티 권한을 받은 매니저
    private val cameraManager by lazy {
        activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager }

    // 내가 카메라 매니저로 카메라의 질의를 담는 세팅 ex) 내가 렌즈의 어디를 사용할지에 대해 key가 오면 그 질의를 cameraId에 담음
    private fun <T> cameraCharacterristics(cameraId : String, key : CameraCharacteristics.Key<T>): T? {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        return when(key){
            CameraCharacteristics.LENS_FACING -> characteristics.get(key)
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP -> characteristics.get(key)
            else -> throw IllegalArgumentException("Key not Recognized")
        }
    }
    // 내가 사용할 렌즈의 방향을 정하면 그 방향에 해당하는 device의 id값을 결정해주는 역할
    private fun cameraId(lens : Int) : String {
        var deviceId = listOf<String>()
        try{
            val cameraIdList = cameraManager.cameraIdList
            deviceId = cameraIdList.filter { lens == cameraCharacterristics(it, CameraCharacteristics.LENS_FACING) }
        }catch (e : CameraAccessException)
        {
            Log.e(TAG, e.toString())
        }
        return deviceId[0]
    }
    // 내 카메라의 렌즈의 방향을 설정을 위한 연결함수
    private fun connectCamera(){
        val deviceId = cameraId(CameraCharacteristics.LENS_FACING_BACK) // 내가 카메라 뒷편을 라는 것을 설정
        Log.d(TAG, "deviceId : $deviceId")
        try{
                cameraManager.openCamera(deviceId, deviceStateCallBack, backgroudHandler)

        }catch (e : CameraAccessException){
            Log.e(TAG, e.toString())
        }catch (e : InterruptedException){
            Log.e(TAG, "camera device Interrupted while opened")
        }
    }

    // 사진찍기
    private lateinit var imageReader : ImageReader
    private lateinit var uri : Uri
    private fun takePicture() = try {
        val map = cameraCharacterristics(cameraId(CameraCharacteristics.LENS_FACING_BACK),
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.getOutputSizes(
            ImageFormat.JPEG
        )
        var width = 640
        var height = 480
        if (map != null && map.isNotEmpty()) {
            width = map[0].width
            height = map[0].height
        }

        imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG,2)
        val outputSurfaces = ArrayList<Surface>(2)
        outputSurfaces.add(imageReader.surface)
        outputSurfaces.add(Surface(textureView!!.surfaceTexture))

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequestBuilder.addTarget(imageReader.surface)
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

        // Orientation
        var rotation = activity!!.windowManager.defaultDisplay.rotation
        captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))
        val file =  File(Environment.getExternalStorageDirectory().toString()+"/pic.jpg")
        uri = file.toUri()
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
                var output: FileOutputStream? = null
                try {
                    output = FileOutputStream(file).apply { write(bytes) }
                } finally {
                    output?.close()
                }
            }
        }
        imageReader.setOnImageAvailableListener(readerListener, backgroudHandler)

        val captureListener = object : CameraCaptureSession.CaptureCallback(){
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
            }
        }
        cameraDevice.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback(){
            override fun onConfigureFailed(session: CameraCaptureSession) {}
            override fun onConfigured(session: CameraCaptureSession) {
                try {
                    session.capture(captureRequestBuilder.build(), captureListener, backgroudHandler)
                }catch (e:CameraAccessException){
                    e.printStackTrace()
                }
            }
        },backgroudHandler)

    } catch (e: CameraAccessException) {
        e.printStackTrace()
    }

    // 화면에 대한 listener
    private val surfaceListener = object: TextureView.SurfaceTextureListener{
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            return false
        }

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture?,
            width: Int,
            height: Int
        ) {
        }
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit
    }

    // 재시작
    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if(textureView.isAvailable)
            openCamera()
        else
            textureView.surfaceTextureListener = surfaceListener
    }

    // 일시정지
    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    // 카메라 권한 + 카메라 열기
    private fun openCamera() {
        connectCamera()
    }

    companion object{
        private val TAG = "CameraFragment"
        private val REQUEST_CAMERA_PERMISSION = 100
        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 0)
            ORIENTATIONS.append(Surface.ROTATION_90, 90)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.pref = context.getSharedPreferences("ImageData",0) // 0은 MODE_PRIVATE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        take_btn.setOnClickListener {
            takePicture()
        }
        upload_btn.setOnClickListener {
            uploadFile()
        }
        cancel_btn.setOnClickListener {
            findNavController().navigate(R.id.to_camera)
        }
    }

    //내부 저장 DB
    private lateinit var pref : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor
    private var realDB = FirebaseDatabase.getInstance().getReference("Users")
    private fun uploadFile() {
        val user = pref.getString("current_user", null)
        //업로드할 파일이 있으면 수행)
        if (uri != null && user != null) {
            //업로드 진행 Dialog 보이기
            val progressDialog = ProgressDialog(this.context)
            progressDialog.setTitle("업로드중...")
            progressDialog.show()

            //storage
            val storage = FirebaseStorage.getInstance()
            //Unique한 파일명을 만들자.
            val formatter = SimpleDateFormat("yyyyMMdd")
            val now = Date()
            editor = pref.edit()

            //날짜가 바뀔 때마다 imageCount 초기화 및 날짜 초기화 하여 파일명 생성
            //첫 if는 이미 데이터가 저장되어있는경우
            if(pref.getString("pivotDate","0") != null){
                // 저장되어있는 날짜와 현재의 날짜가 다르면 날짜가 바뀐 것 -> imageCount 초기화, 기준 날짜 오늘로 저장
                if(pref.getString("pivotDate","0") != formatter.format(now)){
                    editor.putString("pivotDate", formatter.format(now))
                    editor.putInt("imageCount",0)
                    editor.putInt("documentCount",0)
                    editor.apply()
                }
            }
            // 아무것도 저장이 안되어있는 경우 초기화 진행
            else {
                editor.putString("pivotDate", formatter.format(now))
                editor.putInt("imageCount", 0)
                editor.putInt("documentCount", 0)
                editor.apply()
            }

            var imageCount = pref.getInt("imageCount", 0)
            val date = formatter.format(now)
            // 파일명을 날짜에 갯수를 더한 형태로 저장
            val filename = formatter.format(now) + "$imageCount" +".png"
            imageCount += 1
            editor.putInt("imageCount", imageCount)
            editor.apply()
            //storage 주소와 폴더 파일명을 지정해 준다.
            val storageRef = storage.getReferenceFromUrl("gs://test-e80f4.appspot.com")
                .child("/$date/$user/$filename")

            //올리기
            storageRef.putFile(uri)
                //성공시
                .addOnSuccessListener {
                    progressDialog.dismiss() //업로드 진행 Dialog 상자 닫기
                    setUrl()
                    findNavController().navigate(R.id.to_home)
                }
                //실패시
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(context,"업로드 실패",Toast.LENGTH_SHORT).show()
                }
                //진행중
                .addOnProgressListener { taskSnapshot ->
                    val progress =
                        100 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount//이걸 넣어 줘야 아랫줄에 에러가 사라진다
                    //dialog에 진행률을 퍼센트로 출력해 준다
                    progressDialog.setMessage("Uploaded " + progress.toInt() + "% ...")
                }

        } else {
            Toast.makeText(context, "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun findRef() : DatabaseReference?{
        val email = pref.getString("current_user", null)
        val db = realDB
        db.orderByChild("email").equalTo("$email").ref.parent
        return db
    }

    private fun setUrl(){
        val editor = pref.edit()
        val formatter = SimpleDateFormat("yyyyMMdd")
        val now = Date()
        var docuCount = pref.getInt("documentCount", 0)
        val imageCount = pref.getInt("imageCount", 0) - 1
        val filename = formatter.format(now) + "$imageCount" +".png"
        var dbref = findRef()?.child("image")?.child(formatter.format(now))?.ref
        val url = FirebaseStorage.getInstance().reference.child(formatter.format(now)).child(filename).downloadUrl.result
        dbref?.setValue(url)
        docuCount += 1
        editor.putInt("documentCount", docuCount)
        editor.apply()
    }

}
