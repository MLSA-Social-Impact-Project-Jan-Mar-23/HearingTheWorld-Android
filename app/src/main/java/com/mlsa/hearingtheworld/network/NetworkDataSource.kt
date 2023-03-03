package com.mlsa.hearingtheworld.network

import com.mlsa.hearingtheworld.preferences.SessionManager
import javax.inject.Inject

class NetworkDataSource @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
) : BaseNetworkDataSource() {



//    suspend fun getSubjects(id: String)= getResult {
//        apiService.getSubjectList(sessionManager.session, id)
//    }
//
//    suspend fun getNotes(id: String)= getResult {
//        apiService.getNotes(sessionManager.session, id)
//    }
//    suspend fun getMockTests(id: String)= getResult {
//        apiService.getMockTests(sessionManager.session, id)
//    }
////    suspend fun getQuestions(id: String)= getResult {
////        apiService.getQuestions(sessionManager.session, id)
////    }
//    suspend fun getQuestions(id: String)= getResult {
//        apiService.getQuestions(sessionManager.session, id)
//    }
//    suspend fun getForumQuestions()= getResult {
//        apiService.forumQuestions(sessionManager.session)
//    }



}