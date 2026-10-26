package com.example.aplicativocicli.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.time.LocalDate

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val ex = intent.extras ?: return
        if (ex.getInt("attempt") == 0) {
            val next = Bundle(ex).apply { putLong("scheduled_at", alarme_agenda.nextTime(getInt("hour"), getInt("minute"))) }
            alarme_agenda.schedule(context, next.getLong("scheduled_at"), next, ex.getInt("alarm_id"))
            val start = LocalDate.parse(ex.getString("start_date"))
            if (dia_pausa.e_pausa(start, ex.getInt("active"), ex.getInt("placebo"), ex.getInt("pause"))) return
        }
        notificacao.show(context, ex)
    }
}