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

    fun createSession(userId: Int, name: String?, mobile: String?, bkashMobileNumber: String?) {

        pref.edit {
            putBoolean("isLogin", true)
            putInt("userId", userId)
            putString("username", name)
            putString("mobile", mobile)
            putString("bkashMobileNumber", bkashMobileNumber)
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

    var isOffline: Boolean
        get() {
            return pref.getBoolean("isOffline", false)
        }
        set(value) {
            pref.edit {
                putBoolean("isOffline", value)
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

    var bkashMobileNumber: String
        get() {
            return pref.getString("bkashMobileNumber", "")!!
        }
        set(value) {
            pref.edit {
                putString("bkashMobileNumber", value)
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

    var dtUserId: Int
        get() {
            return pref.getInt("dtUserId", 0)
        }
        set(value) {
            pref.edit {
                putInt("dtUserId", value)
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

    var userPic: String
        get() {
            return pref.getString("userPic", "")!!
        }
        set(value) {
            pref.edit {
                putString("userPic", value)
            }
        }

    var riderType: String
        get() {
            return pref.getString("riderType", "")!!
        }
        set(value) {
            pref.edit {
                putString("riderType", value)
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

    var firebaseToken: String
        get() {
            return pref.getString("firebaseToken", "")!!
        }
        set(value) {
            pref.edit {
                putString("firebaseToken", value)
            }
        }

    var deviceId: String
        get() {
            return pref.getString("deviceId", "")!!
        }
        set(value) {
            pref.edit {
                putString("deviceId", value)
            }
        }

    var profileSignature: String
        get() {
            return pref.getString("profileSignature", "")!!
        }
        set(value) {
            pref.edit {
                putString("profileSignature", value)
            }
        }
    var nidSignature: String
        get() {
            return pref.getString("nidSignature", "")!!
        }
        set(value) {
            pref.edit {
                putString("nidSignature", value)
            }
        }
    var licenseSignature: String
        get() {
            return pref.getString("licenseSignature", "")!!
        }
        set(value) {
            pref.edit {
                putString("licenseSignature", value)
            }
        }

    var workManagerUUID: String
        get() {
            return pref.getString("workManagerUUID", "")!!
        }
        set(value) {
            pref.edit {
                putString("workManagerUUID", value)
            }
        }

    var isLocationConsentShown: Boolean
        get() {
            return pref.getBoolean("locationConsent", false)
        }
        set(value) {
            pref.edit {
                putBoolean("locationConsent", value)
            }
        }
}