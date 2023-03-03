package com.mlsa.hearingtheworld.network


//FOR FLOW
/*sealed class Resource<T>(
    val data: T? = null,
    val error: String? = null,
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(throwable: String, data: T? = null) : Resource<T>(data, throwable)
}*/


data class Resource<out T>(val status: Status, val data: T?, val message: String?){

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    companion object {
        fun <T> success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> loading(data: T? =null): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }

        fun <T> error(message: String?, data: T? = null): Resource<T> {
            return Resource(Status.ERROR, data, message)
        }
    }
}
