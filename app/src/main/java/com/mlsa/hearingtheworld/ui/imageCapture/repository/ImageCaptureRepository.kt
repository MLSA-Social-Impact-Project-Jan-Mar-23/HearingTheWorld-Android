package com.mlsa.hearingtheworld.ui.imageCapture.repository

import com.mlsa.hearingtheworld.network.ApiService
import com.mlsa.hearingtheworld.preferences.BaseRepository
import okhttp3.MultipartBody
import javax.inject.Inject

class ImageCaptureRepository @Inject constructor(
private val api: ApiService
): BaseRepository() {

    suspend fun generateStory(
        attachment: MultipartBody.Part
    )= safeApiCall { api.generate(attachment) }

}