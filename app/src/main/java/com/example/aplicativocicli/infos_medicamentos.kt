package com.example.aplicativocicli

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.aplicativocicli.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class infoPilula : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infos_medicamentos)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { finish() }

        val id = intent.getLongExtra("pill_id", -1)

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.get(this@infoPilula).pilulaDao()
            val pilula = dao.buscarPorId(id)
            val principios = dao.principiosAtivos(id)
            val evitar = dao.medicamentosEvitar(id)

            withContext(Dispatchers.Main) {
                if (pilula == null) {
                    finish()
                    return@withContext
                }
                findViewById<TextView>(R.id.txt_nome).text = pilula.medName
                findViewById<TextView>(R.id.txt_principio).text = principios.joinToString(" + ") { it.replaceFirstChar { c -> c.uppercase() } }
                findViewById<TextView>(R.id.txt_atraso).text = pilula.missHours?.let { "Até $it horas" } ?: "Consulte a bula"
                findViewById<TextView>(R.id.txt_evitar).text = if (evitar.isEmpty()) "Nenhum cadastrado" else evitar.joinToString(", ") { it.name }
            }
        }
    }
}