package com.example.aplicativocicli.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.example.aplicativocicli.R
import android.os.Handler
import android.os.Looper

object som_alarme {
    private var player: MediaPlayer? = null

    private val musicas = listOf(
        R.raw.animal_crossing,
        R.raw.pokemon,
        R.raw.mario,
        R.raw.zelda
    )

    fun tocar(context: Context, indice: Int = 0) {
        if (player != null) return
        val res = musicas.getOrElse(indice) { musicas[0] }
        val uri = Uri.parse("android.resource://${context.packageName}/$res")
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
    private var previewPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    fun preview(context: Context, indice: Int, segundos: Long = 10) {
        pararPreview()
        val res = musicas.getOrElse(indice) { musicas[0] }
        previewPlayer = MediaPlayer.create(context.applicationContext, res)?.apply { start() }
        handler.postDelayed({ pararPreview() }, segundos * 1000)
    }

    fun pararPreview() {
        handler.removeCallbacksAndMessages(null)
        previewPlayer?.run {
            if (isPlaying) stop()
            release()
        }
        previewPlayer = null
    }
}