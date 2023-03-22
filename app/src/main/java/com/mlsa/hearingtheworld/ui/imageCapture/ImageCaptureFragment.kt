package com.mlsa.hearingtheworld.ui.imageCapture

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mlsa.hearingtheworld.databinding.ImageCaptureFragmentBinding
import com.mlsa.hearingtheworld.network.Resource
import com.mlsa.hearingtheworld.network.UploadRequestBody
import com.mlsa.hearingtheworld.preferences.SessionManager
import com.mlsa.hearingtheworld.ui.imageCapture.viewModel.ImageCaptureViewModel
import com.mlsa.hearingtheworld.utils.autoCleared
import com.mlsa.hearingtheworld.utils.getFileName
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MultipartBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject


@AndroidEntryPoint
class ImageCaptureFragment : Fragment(), TextToSpeech.OnInitListener,
UploadRequestBody.UploadCallBack{

    private var binding: ImageCaptureFragmentBinding by autoCleared()
    private val viewModel: ImageCaptureViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService
    private var tts: TextToSpeech? = null

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ImageCaptureFragmentBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObserver()

        tts = TextToSpeech(requireContext(), this)

        if (allPermissionsGranted()) {
            startCamera()
             } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

//            speakOut("Clicking picture in 3, 2, 1")
//            takePhoto()

        binding.viewFinder.setOnClickListener {
            if (allPermissionsGranted()) {
                speakOut("Clicking picture in 3, 2, 1")
                Handler(Looper.getMainLooper()).postDelayed({
                    takePhoto()
                },3000)
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                )
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()



    }


    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    //todo: send image to server
                    var selectedImageUri=output.savedUri
                    if (output.savedUri != null) {
                        val parcelFileDescriptor =
                            requireContext().contentResolver.openFileDescriptor(selectedImageUri!!, "r", null)
                                ?: return

                        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                        val file = File(
                            requireContext().cacheDir,
                            requireContext().contentResolver.getFileName(selectedImageUri)
                        )
                        val outputStream = FileOutputStream(file)
                        inputStream.copyTo(outputStream)

                        val body = UploadRequestBody(file, "image", this@ImageCaptureFragment)
                        viewModel.generateStory(
                            MultipartBody.Part.createFormData("image", file.name, body)
                        )
                    }
                    //todo: open bottom sheet
                    //ResultBottomSheetFragment().show(requireActivity().supportFragmentManager, "newStory")
                    //todo: load the result
                    //todo: speak out
                    //Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

                }
            }
        )
    }



    private fun startCamera() {
        Handler(Looper.getMainLooper()).postDelayed({
            speakOut("Welcome, ${sessionManager.userName}! Your camera is ready, Please  tap anywhere on the screen to take photo.")
        },3000)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            imageCapture = ImageCapture.Builder().build()


            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun setupObserver() {
        viewModel.storyResponse.observe(viewLifecycleOwner) {
            it?.let {
                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        it.data?.let { storyResponse ->
                            speakOut(storyResponse.story)
                            //binding.storyText.text = storyResponse.story
                        }

                    }
                    Resource.Status.ERROR -> {

                    }
                    Resource.Status.LOADING -> {

                    }
                }
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        // Shutdown TTS when
        // activity is destroyed
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
    }

    companion object {
        private const val TAG = "HearingTheWorldApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                //finish()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            } else {

            }
            tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String) {
                    binding.viewFinder.isClickable= true
                }

                override fun onError(utteranceId: String) {}
                override fun onStart(utteranceId: String) {
                    binding.viewFinder.isClickable= false

                }
            })
        } else{
            Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakOut(text:String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
    }

    override fun onProgressUpdate(percentage: Int) {

    }
}


