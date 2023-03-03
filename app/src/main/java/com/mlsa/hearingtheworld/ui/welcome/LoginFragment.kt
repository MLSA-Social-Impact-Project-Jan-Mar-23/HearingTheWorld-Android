package com.mlsa.hearingtheworld.ui.welcome

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.camera.video.*
import androidx.fragment.app.Fragment
import com.mlsa.hearingtheworld.MainActivity
import com.mlsa.hearingtheworld.databinding.ImageCaptureFragmentBinding
import com.mlsa.hearingtheworld.databinding.LoginFragmentBinding
import com.mlsa.hearingtheworld.preferences.SessionManager
import com.mlsa.hearingtheworld.utils.autoCleared
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var binding: LoginFragmentBinding by autoCleared()

    @Inject
    lateinit var sessionManager: SessionManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LoginFragmentBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        requireActivity().window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )

        binding.btnNext.setOnClickListener {
            if (binding.firstName.editText?.text.isNullOrBlank()) {
                binding.firstName.error = "Please add name"
                return@setOnClickListener
            }
            sessionManager.userName = binding.firstName.editText?.text.toString()
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()

        }


    }
}


