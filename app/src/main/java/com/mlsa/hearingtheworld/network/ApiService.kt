package com.mlsa.hearingtheworld.network


import com.mlsa.hearingtheworld.data.response.StoryResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @Multipart
    @POST("/generate")
    suspend fun generate(
        @Part attachment: MultipartBody.Part
    ): StoryResponse


}