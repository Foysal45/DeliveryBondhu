package com.ajkerdeal.app.essential.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.NonNull
import androidx.core.content.edit

object SessionManager {

    private const val prefName = "com.ajkerdeal.app.essential.session"
    private lateinit var pref: SharedPreferences

    fun init(@NonNull context: Context) {
        pref = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }

    fun createSession(userId: Int, name: String?, mobile: String?, token: String) {

        pref.edit {
            putBoolean("isLogin", true)
            putInt("userId", userId)
            putString("username", name)
            putString("mobile", mobile)
            putString("accessToken", token)
        }
    }

    fun saveAuthCredentials(userName:String, password:String, isRemember: Boolean) {
        pref.edit {
            putBoolean("isRemember", isRemember)
            putString("userNameLogin", userName)
            putString("password", password)
        }
    }

    var isRemember: Boolean
        get() {
            return pref.getBoolean("isRemember", false)
        }
        set(value) {
            pref.edit {
                putBoolean("isRemember", value)
            }
        }

    var userNameLogin: String
        get() {
            return pref.getString("userNameLogin", "")!!
        }
        set(value) {
            pref.edit {
                putString("userNameLogin", value)
            }
        }

    var password: String
        get() {
            return pref.getString("password", "")!!
        }
        set(value) {
            pref.edit {
                putString("password", value)
            }
        }

    fun clearSession() {
        pref.edit {
            clear()
        }
    }

    var isLogin: Boolean
        get() {
            return pref.getBoolean("isLogin", false)
        }
        set(value) {
            pref.edit {
                putBoolean("isLogin", value)
            }
        }

    var accessToken: String
        get() {
            return pref.getString("accessToken", "")!!
        }
        set(value) {
            pref.edit {
                putString("accessToken", value)
            }
        }

    var mobile: String
        get() {
            return pref.getString("mobile", "")!!
        }
        set(value) {
            pref.edit {
                putString("mobile", value)
            }
        }

    var userId: Int
        get() {
            return pref.getInt("userId", 0)
        }
        set(value) {
            pref.edit {
                putInt("userId", value)
            }
        }

    var userName: String
        get() {
            return pref.getString("username", "")!!
        }
        set(value) {
            pref.edit {
                putString("username", value)
            }
        }



    var lastSyncedStamp: Long
        get() {
            return pref.getLong("lastSyncedStamp", 0)
        }
        set(value) {
            pref.edit {
                putLong("lastSyncedStamp", value)
            }
        }

}