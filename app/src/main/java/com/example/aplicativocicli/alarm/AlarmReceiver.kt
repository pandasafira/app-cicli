package com.example.aplicativocicli.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val ex = intent.extras ?: return
        if (ex.getInt("attempt") == 0) {
            val hora = ex.getInt("hour")
            val minuto = ex.getInt("minute")
            alarme_agenda.schedule(context, alarme_agenda.nextTime(hora, minuto), Bundle(ex), ex.getInt("alarm_id"))
            val start = LocalDate.parse(ex.getString("start_date"))
            if (dia_pausa.e_pausa(start, ex.getInt("active"), ex.getInt("placebo"), ex.getInt("pause"))) return
            ex.putString("scheduled_at", LocalDateTime.of(LocalDate.now(), LocalTime.of(hora, minuto)).toString())
        }
        notificacao.show(context, ex)
    }
}