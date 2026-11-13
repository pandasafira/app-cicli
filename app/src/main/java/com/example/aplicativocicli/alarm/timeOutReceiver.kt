package com.example.aplicativocicli.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.aplicativocicli.data.AppDatabase
import com.example.aplicativocicli.data.DoseStatus
import com.example.aplicativocicli.data.MedicationHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AlarmTimeoutReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val ex = intent.extras ?: return
        val alarmId = ex.getInt("alarm_id")
        val tentativa = ex.getInt("attempt")
        val limite = ex.getInt("limit")
        val intervalo = ex.getInt("interval")

        som_alarme.parar()
        notificacao.cancelar(context, alarmId)

        if (intervalo > 0 && tentativa + 1 < limite) {
            alarme_agenda.agendarRepeticao(context, ex)
            return
        }

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.get(context)
                val usuario = db.userDao().buscarUsuario()
                val cartela = db.pillPackDao().cartelaAtual()
                val agora = LocalDateTime.now().withNano(0).toString()
                val previsto = ex.getString("scheduled_at") ?: agora
                if (usuario != null && cartela != null) {
                    db.historicoDao().inserir(
                        MedicationHistory(
                            userId = usuario.id,
                            packId = cartela.id,
                            alarmId = alarmId.toLong(),
                            scheduledAt = previsto,
                            confirmedAt = null,
                            status = DoseStatus.NOT_TAKEN,
                            createdAt = agora
                        )
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }
}