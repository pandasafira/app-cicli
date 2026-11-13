package com.example.aplicativocicli.alarm

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.aplicativocicli.R

object notificacao {
    const val CHANNEL_ID = "cicli_alarm_v2"
    private const val TEMPO_TOCANDO = 60_000L
    private const val OFFSET_TIMEOUT = 200000

    private fun pendingTimeout(context: Context, alarmId: Int, extras: Bundle? = null): PendingIntent {
        val intent = Intent(context, AlarmTimeoutReceiver::class.java)
        if (extras != null) intent.putExtras(extras)
        return PendingIntent.getBroadcast(context, alarmId + OFFSET_TIMEOUT, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Alarme do anticoncepcional", NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(null, null)
            enableVibration(true)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun cancelar(context: Context, alarmId: Int) {
        NotificationManagerCompat.from(context).cancel(alarmId)
        context.getSystemService(AlarmManager::class.java).cancel(pendingTimeout(context, alarmId))
    }

    fun show(context: Context, extras: Bundle) {
        val alarmId = extras.getInt("alarm_id")
        val intent = Intent(context, AlarmActivity::class.java).putExtras(extras).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pi = PendingIntent.getActivity(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Hora do anticoncepcional 💊")
            .setContentText("Toque para confirmar")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pi, true)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            som_alarme.tocar(context, extras.getInt("sound_index"))
            NotificationManagerCompat.from(context).notify(alarmId, notification)
            val am = context.getSystemService(AlarmManager::class.java)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TEMPO_TOCANDO, pendingTimeout(context, alarmId, extras))
        }
    }
}
