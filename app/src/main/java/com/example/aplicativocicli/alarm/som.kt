package com.example.aplicativocicli.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import com.example.aplicativocicli.R

object som_alarme {
    private var player: MediaPlayer? = null

    fun tocar(context: Context) {
        if (player != null) return
        val uri = Uri.parse("android.resource://${context.packageName}/${R.raw.animal_crossing}")
        player = MediaPlayer().apply {
            setWakeMode(context.applicationContext, android.os.PowerManager.PARTIAL_WAKE_LOCK)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(context.applicationContext, uri)
            isLooping = true
            prepare()
            start()
        }
    }

    fun parar() {
        player?.run {
            if (isPlaying) stop()
            release()
        }
        player = null
    }
}