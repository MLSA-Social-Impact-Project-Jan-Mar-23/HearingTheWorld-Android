package com.mlsa.hearingtheworld.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.mlsa.hearingtheworld.network.Resource
import kotlinx.coroutines.Dispatchers


//inline makes higher-order fun more efficient
//inline fun <ResultType, RequestType> networkBoundResource(
//    crossinline databaseQuery: () -> Flow<ResultType>,
//    crossinline networkCall: suspend () -> RequestType,
//    crossinline saveCallResult: suspend (RequestType) -> Unit,
//    crossinline onFetchFailed: (Throwable) -> Unit = { Unit },
//    crossinline shouldCall: (ResultType) -> Boolean = { true }
//) =
//    flow<Resource<ResultType>> {
//        val data = databaseQuery().first()
//        val flow = if (shouldCall(data)) { //decide if its time to fetch new data
//            emit(Resource.Loading(data))
//            try {
//                saveCallResult(networkCall())
//                databaseQuery().map { Resource.Success(it) }
//            } catch (throwable: Throwable) {
//                //added checkThrowable function
//                //checkThrowable<ResultType>(throwable)
//                onFetchFailed(throwable)
//
//                databaseQuery().map { Resource.Error(throwable.localizedMessage, it) }
//            }
//        } else {
//            databaseQuery().map { Resource.Success(it) }
//        }
//        emitAll(flow)
//    }
//
//fun <T> checkThrowable(throwable: Throwable): Resource.Error<T> {
//    when (throwable) {
//        is HttpException -> {
//            when (throwable.code()) {
//                400 -> {
//                    return Resource.Error<T>("Wrong Credentials")
//                }
//                401 -> {
//                    return Resource.Error<T>("Unauthorized")
//                }
//                408 -> {
//                    return Resource.Error<T>("Request Timeout")
//                }
//                500 -> {
//                    return Resource.Error("Server Down")
//                }
//                else -> {
//                    return Resource.Error<T>(throwable.localizedMessage)
//                }
//            }
//        }
//        else -> {
//            return Resource.Error<T>("No Connectivity! Check your internet Connection!")
//        }
//    }
//}


fun <T, A> networkBoundResource(
    databaseQuery: () -> LiveData<T>,
    networkCall: suspend () -> Resource<A>,
    saveCallResult: suspend (A) -> Unit
): LiveData<Resource<T>> =
    liveData(Dispatchers.IO) {
        emit(Resource.loading<T>())
        val source = databaseQuery.invoke().map { Resource.success(it) }
        emitSource(source)

        val responseStatus = networkCall.invoke()

        /*try {
            saveCallResult(networkCall.invoke().data!!)
        }catch (throwable: Throwable){
            when (throwable) {
                is HttpException -> {
                    when (throwable.code()) {
                        400 -> {
                            emit(Resource.error("Wrong Credentials"))
                        }
                        401 -> {
                            emit(Resource.error("Unauthorized"))
                        }
                        408 -> {
                            emit(Resource.error("Request Timeout"))
                        }
                        500 -> {
                            emit(Resource.error("Server Down"))
                        }
                        else -> {
                            emit(Resource.error(throwable.message))
                        }
                    }
                }
                else -> {
                    emit(Resource.error("No Connectivity! Check your internet Connection!"))

                }
            }
            emitSource(source)
        }*/

        if (responseStatus.status == Resource.Status.SUCCESS) {
            saveCallResult(responseStatus.data!!)
        } else if (responseStatus.status == Resource.Status.ERROR) {
            emit(Resource.error<T>(responseStatus.message!!))

           /* when (responseStatus) {
                is HttpException -> {
                    when (responseStatus.code()) {
                        400 -> {
                            emit(Resource.error("Wrong Credentials"))
                        }
                        401 -> {
                            emit(Resource.error("Unauthorized"))
                        }
                        408 -> {
                            emit(Resource.error("Request Timeout"))
                        }
                        500 -> {
                            emit(Resource.error("Server Down"))
                        }
                        else -> {
                            emit(Resource.error(responseStatus.localizedMessage))
                        }
                    }
                }
                else -> {
                    emit(Resource.error("No Connectivity! Check your internet Connection!"))

                }
            }*/

            emitSource(source)
        }
    }