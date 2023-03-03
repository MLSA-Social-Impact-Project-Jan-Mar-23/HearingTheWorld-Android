package com.mlsa.hearingtheworld.preferences

import android.content.Context
import android.content.SharedPreferences
import com.mlsa.hearingtheworld.HearingTheWorldApplication.Companion.appContext
import com.mlsa.hearingtheworld.R
import javax.inject.Inject

class SessionManager @Inject constructor() {
    private val context = appContext!!

    private var prefs: SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE)

    companion object {
        var SESSION = "session"
        var USERID = "userId"
        var USERNAME = "userName"

    }


    var session: String
        set(value) {
            prefs.edit()
                .putString(SESSION, value)
                .apply()
        }
        get() {
            return prefs.getString(SESSION, "") ?: ""
        }
    var userId: Int
        set(value) {
            prefs.edit()
                .putInt(USERID, value)
                .apply()
        }
        get() {
            return prefs.getInt(USERID, 0) ?: 0
        }

    var userName: String
        set(value) {
            prefs.edit()
                .putString(USERNAME, value)
                .apply()
        }
        get() {
            return prefs.getString(USERNAME, "") ?: ""
        }


    fun clearAuthSession() {
        val editor = prefs.edit()
        editor.putString(SESSION, null)
        editor.putString(USERNAME, null)
        editor.putInt(USERID, 0)
        editor.apply()
    }


}