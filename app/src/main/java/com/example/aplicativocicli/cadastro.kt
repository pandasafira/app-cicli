package com.example.aplicativocicli

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplicativocicli.data.AppDatabase
import com.example.aplicativocicli.data.PillPack
import com.example.aplicativocicli.data.User
import com.example.aplicativocicli.data.BirthPill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

class cadastro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro)

        val botao_main = findViewById<Button>(R.id.botao_concluir)

        // inputs do cadastros
        val nome = findViewById<EditText>(R.id.edit_nome)
        val data_nasc = findViewById<EditText>(R.id.edit_nascimento)
        val anticoncepcional = findViewById<AutoCompleteTextView>(R.id.edit_anticoncepcional)
        val ini_cartela = findViewById<EditText>(R.id.edit_inicio_cartela)
        val cartela = findViewById<Spinner>(R.id.spinner_cartela)
        val cartelas = listOf(
            Triple(21, 0, 7),
            Triple(22, 0, 6),
            Triple(24, 0, 4),
            Triple(28, 0, 2),
            Triple(21, 7, 0),
            Triple(24, 4, 0),
            Triple(84, 0, 7),
            Triple(28, 0, 0)
        )

        var pilulas: List<BirthPill> = emptyList()

        lifecycleScope.launch(Dispatchers.IO) {
            pilulas = AppDatabase.get(this@cadastro).pilulaDao().listar()
            withContext(Dispatchers.Main) {
                val nomes = pilulas.map { it.medName }
                anticoncepcional.setAdapter(
                    ArrayAdapter(
                        this@cadastro,
                        android.R.layout.simple_dropdown_item_1line,
                        nomes
                    )
                )
            }
        }

        fun tela_inicial() {
            getSharedPreferences("cicli", MODE_PRIVATE).edit().putBoolean("cadastrado", true)
                .apply()
            startActivity(Intent(this, main::class.java))
            finish()
        }

        fun valida_campo_nome(nome: String): Boolean {
            if (nome.isBlank() || !nome.matches(Regex("^[A-Za-zÀ-ÿ ]+$"))) {
                return false
            }
            return true
        }

        fun valida_campo_data(data: String): Boolean {
            val formato_padrao = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

            try {
                val data_formatada = LocalDate.parse(data, formato_padrao)
                val ano_hoje = LocalDate.now().year
                if (data_formatada.year >= ano_hoje) {
                    return false
                } else {
                    return true
                }
            } catch (e: Exception) {
                return false
            }
            return true
        }

        botao_main.setOnClickListener {
            val pilula = pilulas.find { it.medName == anticoncepcional.text.toString() }
            if (valida_campo_nome(nome.text.toString()) == false) {
                Toast.makeText(this, "Confira seu nome", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (valida_campo_data(data_nasc.text.toString()) == false) {
                Toast.makeText(
                    this, "Confira sua data de nascimento",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            } else {
            val user_banco = User(
                name = nome.text.toString(),
                birthDate = data_nasc.text.toString(),
                createdAt = LocalDate.now().toString()
            )
            val (ativos, placebo, pausa) = cartelas[cartela.selectedItemPosition]
            val inicio = LocalDate.parse(ini_cartela.text.toString(), DateTimeFormatter.ofPattern("dd/MM/uuuu")).toString()

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.get(this@cadastro)
                val userId = db.userDao().inserir(user_banco)
                db.pillPackDao().inserir(
                    PillPack(
                        userId = userId,
                        birthPillId = pilula?.id,
                        activeDays = ativos,
                        placeboDays = placebo,
                        pauseDays = pausa,
                        startDate = inicio,
                        isCurrent = true,
                        createdAt = LocalDate.now().toString()
                    )
                )
                withContext(Dispatchers.Main) {
                    tela_inicial()
                    }
                }

            }

        }
    }
}