package com.looperr.app.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.looperr.app.R
import com.looperr.app.data.SpotifyRepository
import com.looperr.app.ui.MainActivity

class LoopService : Service() {

    private val binder = LoopBinder()
    lateinit var loopController: LoopController
        private set

    inner class LoopBinder : Binder() {
        fun getService(): LoopService = this@LoopService
    }

    override fun onCreate() {
        super.onCreate()
        loopController = LoopController(SpotifyRepository(this))
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        loopController.destroy()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Loop Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for loop playback"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Looperr")
            .setContentText("Loop is active")
            .setSmallIcon(R.drawable.ic_loop)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "looperr_loop_channel"
        const val NOTIFICATION_ID = 1
    }
}
