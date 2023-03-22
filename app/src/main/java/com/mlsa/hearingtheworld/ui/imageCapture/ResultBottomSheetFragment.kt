package com.mlsa.hearingtheworld.ui.imageCapture

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.camera.video.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mlsa.hearingtheworld.R
import com.mlsa.hearingtheworld.databinding.ResultBottomSheetFragmentBinding
import com.mlsa.hearingtheworld.network.Resource
import com.mlsa.hearingtheworld.preferences.SessionManager
import com.mlsa.hearingtheworld.ui.imageCapture.viewModel.ImageCaptureViewModel
import com.mlsa.hearingtheworld.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class ResultBottomSheetFragment : BottomSheetDialogFragment(),  TextToSpeech.OnInitListener {

    private var binding: ResultBottomSheetFragmentBinding by autoCleared()

        private val viewModel: ImageCaptureViewModel by viewModels()
    @Inject
    lateinit var sessionManager: SessionManager
    private var tts: TextToSpeech? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ResultBottomSheetFragmentBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        tts = TextToSpeech(requireContext(), this)

        binding.progressCircular.visibility= View.VISIBLE
        setUpObserver()


        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setUpObserver() {

        viewModel.storyResponse.observe(viewLifecycleOwner){
            it?.let {
                when(it.status){
                    Resource.Status.SUCCESS -> {
                        it.data?.let { storyResponse ->
                            speakOut(storyResponse.story)
                            binding.storyText.text= storyResponse.story
                        }

                    }
                    Resource.Status.ERROR -> {

                    }
                    Resource.Status.LOADING -> {

                    }
                }
            }
        }
        //todo: recieve text
//        Handler(Looper.getMainLooper()).postDelayed({
//            binding.progressCircular.visibility= View.GONE
//            speakOut(getString(R.string.short_story_example))
//        },3000)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language not supported!")
            } else {

            }
        } else {
            Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
}