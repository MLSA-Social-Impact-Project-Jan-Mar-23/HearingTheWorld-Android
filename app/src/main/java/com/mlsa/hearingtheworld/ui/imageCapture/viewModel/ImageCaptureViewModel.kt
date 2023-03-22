package com.mlsa.hearingtheworld.ui.imageCapture.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mlsa.hearingtheworld.data.response.StoryResponse
import com.mlsa.hearingtheworld.network.Resource
import com.mlsa.hearingtheworld.ui.imageCapture.repository.ImageCaptureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class ImageCaptureViewModel @Inject constructor(
    private val imageCaptureRepository: ImageCaptureRepository
) : ViewModel() {

    private val _storyResponse: MutableLiveData<Resource<StoryResponse>> = MutableLiveData()
    val storyResponse: LiveData<Resource<StoryResponse>>
        get() = _storyResponse

    fun generateStory(
        attachment: MultipartBody.Part? = null
    ) = viewModelScope.launch {
        attachment?.let {
            _storyResponse.value = imageCaptureRepository.generateStory(attachment)
        }
    }
}
