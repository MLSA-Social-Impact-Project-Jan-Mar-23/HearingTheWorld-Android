package com.mlsa.hearingtheworld.network

import com.mlsa.hearingtheworld.network.Resource
import retrofit2.Response
import timber.log.Timber

abstract class BaseNetworkDataSource(){


    protected suspend fun <T> getResult(call: suspend () -> Response<T>): Resource<T> {
        try {
            val response = call()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null)
                    return Resource.success(body)
            }else{
                //NO SUCCESS
                when (response.code()) {
                    400 -> {
                        return error("Wrong Credentials")

                    }
                    401 -> {
                        return error("Unauthorized Access")
                    }
                    408 -> {
                        return error("Request Timeout")
                    }
                    500 -> {
                        return error(" Server Down! Please Standby!")
                    }
                }
            }
            return error(response.message())
        } catch (e: Exception){
            //NO NETWORK
            return error("No Internet Connectivity!")
        }
    }

    private fun <T> error(message: String): Resource<T> {
        Timber.e(message)
        return Resource.error("Network Call Failed: $message")
    }
}