package com.mlsa.hearingtheworld.preferences

import com.mlsa.hearingtheworld.network.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

abstract class BaseRepository {

    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is HttpException -> {
                        when (throwable.code()) {
                            400 -> {
                                Resource.error("Wrong Credentials")
                            }
                            401 -> {
                                Resource.error("Unauthorized")
                            }
                            408 -> {
                                Resource.error("Request Timeout")
                            }
                            500 -> {
                                Resource.error("Server Down")
                            }
                            else -> {
                                Resource.error(throwable.localizedMessage)
                            }
                        }
                    }
                    else -> {
                        Resource.error("No Connectivity! Check your internet Connection!")

                    }
                }
            }
        }

    }
}