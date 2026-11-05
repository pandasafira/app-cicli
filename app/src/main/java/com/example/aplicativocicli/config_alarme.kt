package com.example.aplicativocicli

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.aplicativocicli.data.Alarm
import com.example.aplicativocicli.alarm.alarme_agenda
import com.example.aplicativocicli.alarm.notificacao
import com.example.aplicativocicli.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch

class config_alarme : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.config_alarme)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        fun abrir_alarme(){
            startActivity(Intent(this, alarme::class.java))
        }

        val botao_novo = findViewById<Button>(R.id.botao_novo_alarme)

        botao_novo.setOnClickListener{abrir_alarme()}
    }

    override fun onResume() {
        super.onResume()
        carregarAlarmes()
    }

    private fun carregarAlarmes() {
        lifecycleScope.launch(Dispatchers.IO) {
            val alarmes = AppDatabase.get(this@config_alarme).alarmDao().listar()
            withContext(Dispatchers.Main) {
                findViewById<LinearLayout>(R.id.lista_alarmes).removeAllViews()
                alarmes.forEach { adicionarAlarme(it) }
            }
        }
    }

    private fun adicionarAlarme(a: Alarm) {
        val lista = findViewById<LinearLayout>(R.id.lista_alarmes)
        val item = layoutInflater.inflate(R.layout.item_alarme, lista, false)
        item.findViewById<TextView>(R.id.txt_horario).text = "%02d:%02d".format(a.hour, a.minute)
        item.findViewById<TextView>(R.id.txt_intervalo).text = "Intervalo: ${a.intervalMinutes} min"
        item.findViewById<TextView>(R.id.txt_limite).text = "Repetições: ${a.repeatLimit}"
        item.findViewById<ImageButton>(R.id.botao_editar).setOnClickListener {
            startActivity(Intent(this, alarme::class.java).putExtra("alarm_id", a.id))
        }
        item.findViewById<ImageButton>(R.id.botao_excluir).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Excluir alarme")
                .setMessage("Deseja mesmo excluir o alarme das %02d:%02d?".format(a.hour, a.minute))
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        alarme_agenda.cancel(this@config_alarme, a.id.toInt())
                        alarme_agenda.cancelarRepeticao(this@config_alarme, a.id.toInt())
                        notificacao.cancelar(this@config_alarme, a.id.toInt())
                        AppDatabase.get(this@config_alarme).alarmDao().excluir(a)
                        withContext(Dispatchers.Main) { lista.removeView(item) }
                    }
                }
                .show()
        }
        val switch = item.findViewById<MaterialSwitch>(R.id.switch_ativo)
        switch.isChecked = a.active
        switch.setOnCheckedChangeListener { _, ligado ->
            lifecycleScope.launch(Dispatchers.IO) {
                val atualizado = a.copy(active = ligado)
                AppDatabase.get(this@config_alarme).alarmDao().atualizar(atualizado)
                if (ligado) {
                    alarme_agenda.agendar(this@config_alarme, atualizado)
                } else {
                    alarme_agenda.cancel(this@config_alarme, a.id.toInt())
                    alarme_agenda.cancelarRepeticao(this@config_alarme, a.id.toInt())
                    notificacao.cancelar(this@config_alarme, a.id.toInt())
                }
            }
        }
        lista.addView(item)
    }
}