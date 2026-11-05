package com.example.aplicativocicli.alarm

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplicativocicli.R
import com.example.aplicativocicli.data.AppDatabase
import com.example.aplicativocicli.data.DoseStatus
import com.example.aplicativocicli.data.MedicationHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class AlarmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tela_alarme_atividade)

        val ex = intent.extras ?: return finish()
        val alarmId = ex.getInt("alarm_id")
        val tentativa = ex.getInt("attempt")
        val limite = ex.getInt("limit")
        val intervalo = ex.getInt("interval")

        findViewById<TextView>(R.id.txt_tentativa).text = "Lembrete ${tentativa + 1} de ${limite + 1}"

        findViewById<Button>(R.id.btn_tomei).setOnClickListener {
            salvar(ex, DoseStatus.TAKEN)
        }

        findViewById<Button>(R.id.btn_nao_tomei).setOnClickListener {
            if (intervalo > 0 && tentativa + 1 < limite) {
                notificacao.cancelar(this, alarmId)
                alarme_agenda.agendarRepeticao(this, ex)
                Toast.makeText(this, "Vou te lembrar de novo em $intervalo min", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                salvar(ex, DoseStatus.NOT_TAKEN)
            }
        }
    }

    private fun salvar(ex: Bundle, status: DoseStatus) {
        val alarmId = ex.getInt("alarm_id")
        notificacao.cancelar(this, alarmId)
        alarme_agenda.cancelarRepeticao(this, alarmId)

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.get(this@AlarmActivity)
            val usuario = db.userDao().buscarUsuario()
            val cartela = db.pillPackDao().cartelaAtual()
            val agora = LocalDateTime.now().withNano(0).toString()

            if (usuario != null && cartela != null) {
                db.historicoDao().inserir(
                    MedicationHistory(
                        userId = usuario.id,
                        packId = cartela.id,
                        alarmId = alarmId.toLong(),
                        scheduledAt = ex.getString("scheduled_at") ?: agora,
                        confirmedAt = if (status == DoseStatus.TAKEN) agora else null,
                        status = status,
                        createdAt = agora
                    )
                )
            }
            withContext(Dispatchers.Main) { finish() }
        }
    }
}