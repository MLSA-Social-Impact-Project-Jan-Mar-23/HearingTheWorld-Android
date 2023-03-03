package com.mlsa.hearingtheworld.ui.welcome

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.camera.video.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mlsa.hearingtheworld.MainActivity
import com.mlsa.hearingtheworld.databinding.ImageCaptureFragmentBinding
import com.mlsa.hearingtheworld.databinding.SplashFragmentBinding
import com.mlsa.hearingtheworld.preferences.SessionManager
import com.mlsa.hearingtheworld.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var binding: SplashFragmentBinding by autoCleared()
//    private val viewModel: PlotGeoFenceViewModel by viewModels()
    @Inject
lateinit var sessionManager: SessionManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SplashFragmentBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.userName.isEmpty()){
                //goto welcome activity
                findNavController().navigate(
                    SplashFragmentDirections.actionSplashFragmentToLoginFragment()
                )

            }else{
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }, 3000)



    }
}


