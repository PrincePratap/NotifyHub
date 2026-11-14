package com.cody.notifyhub.data.repository

import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class FcmTokenRepository @Inject constructor() {

    /** Returns this device's FCM token, or null if it can't be fetched (e.g. no internet) */
    suspend fun getToken(): String? = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> cont.resume(token) }
            .addOnFailureListener { cont.resume(null) }
    }
}