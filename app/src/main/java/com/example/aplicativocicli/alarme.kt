package com.example.aplicativocicli

import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.aplicativocicli.alarm.alarme_agenda
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.example.aplicativocicli.data.Alarm
import com.example.aplicativocicli.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import android.view.View
import android.widget.AdapterView

class alarme : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.alarme)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            finish()
        }


        val pickerHora = findViewById<NumberPicker>(R.id.pickerHora)
        val pickerMinuto = findViewById<NumberPicker>(R.id.pickerMinuto)

        pickerHora.maxValue = 23
        pickerHora.minValue = 0
        pickerMinuto.maxValue = 59
        pickerMinuto.minValue = 0

        pickerHora.setFormatter{String.format("%02d", it)}
        pickerMinuto.setFormatter {String.format("%02d", it)}

        var botao_salvar = findViewById<Button>(R.id.btnSalvar)
        val spinnerIntervalo = findViewById<Spinner>(R.id.spinnerIntervalo)
        val spinnerLimite = findViewById<Spinner>(R.id.spinnerLimite)
        val switchAtivo = findViewById<SwitchCompat>(R.id.switchAtivo)

        spinnerIntervalo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 6) {
                    spinnerLimite.setSelection(0)
                    spinnerLimite.isEnabled = false
                    spinnerLimite.isClickable = false
                    spinnerLimite.alpha = 0.5f
                } else {
                    spinnerLimite.isEnabled = true
                    spinnerLimite.isClickable = true
                    spinnerLimite.alpha = 1f
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        botao_salvar.setOnClickListener {
            val hora = pickerHora.value
            val minuto = pickerMinuto.value
            val intervalo = spinnerIntervalo.selectedItem.toString().filter { it.isDigit() }.toIntOrNull() ?: 0
            val limite = spinnerLimite.selectedItemPosition + 1
            val ativo = switchAtivo.isChecked

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.get(this@alarme)
                val usuario = db.userDao().buscarUsuario() ?: return@launch

                // salvar no banco
                val novoAlarme = Alarm(
                    userId = usuario.id,
                    hour = hora,
                    minute = minuto,
                    intervalMinutes = intervalo,
                    repeatLimit = limite,
                    active = ativo,
                    createdAt = LocalDate.now().toString()
                )
                val id = db.alarmDao().inserir(novoAlarme).toInt()

                // agendar
                val extras = Bundle().apply {
                    putInt("alarm_id", id)
                    putInt("hour", hora)
                    putInt("minute", minuto)
                    putInt("attempt", 0)
                    putInt("interval", intervalo)
                    putInt("limit", limite)
                    putString("start_date", "2026-10-20")
                    putInt("active", 21)
                    putInt("placebo", 0)
                    putInt("pause", 7)
                }
                if (ativo) {
                    alarme_agenda.schedule(this@alarme, alarme_agenda.nextTime(hora, minuto), extras, id)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@alarme, "Alarme salvo!", Toast.LENGTH_SHORT).show()
                    finish()
                }

            }
        }
    }
}