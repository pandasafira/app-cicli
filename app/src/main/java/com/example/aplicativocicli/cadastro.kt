package com.example.aplicativocicli

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplicativocicli.data.AppDatabase
import com.example.aplicativocicli.data.User
import com.example.projetoappcicli.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
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
        val anticoncepcional = findViewById<EditText>(R.id.edit_anticoncepcional)
        val ini_cartela = findViewById<EditText>(R.id.edit_inicio_cartela)

        fun tela_inicial() {
            getSharedPreferences("cicli", MODE_PRIVATE).edit().putBoolean("cadastrado", true)
                .apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        fun valida_campo_nome(nome: String): Boolean {
            if (nome.isBlank() || !nome.matches(Regex("^[A-Za-zÀ-ÿ ]+$"))) {
                return false
            }
            return true
        }

        fun valida_campo_data(data: String): Boolean {
            val formato_padrao =
                DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
            val data_formatada = LocalDate.parse(data, formato_padrao)
            val ano_hoje = LocalDate.now().year

            try {
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
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.get(this@cadastro).userDao().inserir(user_banco)
                    withContext(Dispatchers.Main) {
                        tela_inicial()
                    }
                }
            }
        }
    }
}