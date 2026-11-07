package com.example.aplicativocicli.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.aplicativocicli.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

class ajuste_celular_desligado : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.get(context)
                val pack = db.pillPackDao().cartelaAtual() ?: return@launch
                val start = pack.startDate ?: return@launch
                Log.d("BOOT", "reagendando alarmes")
                val agora = System.currentTimeMillis()
                val diaHoje = LocalDate.now().toString()
                db.alarmDao().listar().filter { it.active }.forEach { a ->
                    val hoje = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, a.hour)
                        set(Calendar.MINUTE, a.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val perdeu = hoje <= agora && db.historicoDao().contarNoDia(a.id, diaHoje) == 0
                    val disparo = if (perdeu) agora + 5000 else alarme_agenda.nextTime(a.hour, a.minute)
                    val extras = Bundle().apply {
                        putInt("attempt", 0)
                        putInt("alarm_id", a.id.toInt())
                        putInt("hour", a.hour)
                        putInt("minute", a.minute)
                        putInt("interval", a.intervalMinutes)
                        putInt("limit", a.repeatLimit)
                        putLong("scheduled_at", if (perdeu) hoje else disparo)
                        putString("start_date", start)
                        putInt("active", pack.activeDays)
                        putInt("placebo", pack.placeboDays)
                        putInt("pause", pack.pauseDays)
                    }
                    alarme_agenda.schedule(context, disparo, extras, a.id.toInt())
                    Log.d("BOOT", "alarme ${a.id} perdeu=$perdeu")
                }
            } finally {
                pending.finish()
            }
        }
    }
}