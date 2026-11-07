package com.example.aplicativocicli

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.aplicativocicli.data.AppDatabase
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class infoPilula : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infos_medicamentos)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { finish() }

        val id = intent.getLongExtra("pill_id", -1)

        val botaoBula = findViewById<MaterialButton>(R.id.botao_bula)
        botaoBula.setOnClickListener {
            val nome = findViewById<TextView>(R.id.txt_nome).text.toString()
            val url = "https://consultas.anvisa.gov.br/#/bulario/q/?nomeProduto=${Uri.encode(nome)}"
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Não foi possível abrir o navegador", Toast.LENGTH_SHORT).show()
            }
        }

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