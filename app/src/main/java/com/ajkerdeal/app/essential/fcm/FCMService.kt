package com.ajkerdeal.app.essential.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ajkerdeal.app.essential.R
import com.ajkerdeal.app.essential.api.models.auth.fcm.UpdateTokenRequest
import com.ajkerdeal.app.essential.repository.AppRepository
import com.ajkerdeal.app.essential.ui.home.HomeActivity
import com.ajkerdeal.app.essential.utils.SessionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class FCMService : FirebaseMessagingService() {

    private val repository: AppRepository by inject()
    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Timber.d(p0.data.toString())

        val title = p0.data["title"] ?: ""
        val description = p0.data["description"] ?: ""
        val type = p0.data["notificationType"] ?: "0"
        val imageLink = p0.data["imageLink"] ?: ""
        val bigText = p0.data["bigText"] ?: ""


        val jsonElement = gson.toJsonTree(p0.data)
        val fcmModel: FCMData = gson.fromJson(jsonElement, FCMData::class.java)

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("data", fcmModel)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)


        val builder = NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
        with(builder) {
            setSmallIcon(R.drawable.ic_logo_hand)
            setContentTitle(title)
            setContentText(description)
            setAutoCancel(true)
            color = ContextCompat.getColor(this@FCMService, R.color.colorPrimary)
            setDefaults(NotificationCompat.DEFAULT_ALL)
            priority = NotificationCompat.PRIORITY_HIGH
            setContentIntent(pendingIntent)
        }

        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val channel = NotificationChannel(getString(R.string.default_notification_channel_id), "Promotion", NotificationManager.IMPORTANCE_HIGH)
            with(channel) {
                setDescription("Essential delivery offers and promotions")
                setShowBadge(true)
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        when (type) {
            // Default
            "0" -> {
                notificationManager.notify(1, builder.build())
            }
            // Big text
            "1" -> {
                builder.setStyle(NotificationCompat.BigTextStyle().bigText("$description\n$bigText"))
                notificationManager.notify(1, builder.build())
            }
            //Banner
            "2" -> {

                Glide.with(applicationContext)
                    .asBitmap()
                    .load(imageLink)
                    .into(object : CustomTarget<Bitmap?>() {

                        override fun onLoadCleared(placeholder: Drawable?) {
                            notificationManager.notify(1, builder.build())
                        }

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                            val notificationStyle = NotificationCompat.BigPictureStyle()
                            notificationStyle.bigPicture(resource)
                            notificationStyle.bigLargeIcon(null)
                            builder.setStyle(notificationStyle)
                            notificationManager.notify(1, builder.build())
                        }
                    })
            }
            // BigTextWithSideImage
            "3" -> {

                Glide.with(applicationContext)
                    .asBitmap()
                    .load(imageLink)
                    .into(object : CustomTarget<Bitmap?>() {

                        override fun onLoadCleared(placeholder: Drawable?) {
                            notificationManager.notify(1, builder.build())
                        }

                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                            builder.setLargeIcon(resource)
                            builder.setStyle(NotificationCompat.BigTextStyle().bigText("$description\n$bigText"))
                            notificationManager.notify(1, builder.build())
                        }
                    })
            }
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        SessionManager.firebaseToken = p0
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateFirebaseToken(UpdateTokenRequest(SessionManager.userId, p0))
        }
    }
}