package com.example.aplicativocicli

import android.content.Intent
import android.os.Bundle
import android.text.Editable
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
import java.text.Normalizer
import android.text.TextWatcher
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class cadastro : AppCompatActivity() {
    private fun mascaraData(campo: EditText) {
        campo.addTextChangedListener(object : TextWatcher {
            var editando = false
            override fun afterTextChanged(s: Editable?) {
                if (editando) return
                editando = true
                val numeros = s.toString().filter { it.isDigit() }.take(8)
                val formatado = StringBuilder()
                numeros.forEachIndexed { i, c ->
                    if (i == 2 || i == 4) formatado.append('/')
                    formatado.append(c)
                }
                campo.setText(formatado)
                campo.setSelection(formatado.length)
                editando = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(0, 0, 0, teclado.bottom)
            insets
        }

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

        mascaraData(data_nasc)
        mascaraData(ini_cartela)

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

        fun normalizar(texto: String) =
            Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")

        botao_main.setOnClickListener {
            val nomeDigitado = anticoncepcional.text.toString().trim()
            val pilula = pilulas.find { normalizar(it.medName) == normalizar(nomeDigitado) }

            if (valida_campo_nome(nome.text.toString()) == false) {
                Toast.makeText(this, "Confira seu nome", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (nomeDigitado.isBlank()) {
                Toast.makeText(this, "Informe seu anticoncepcional", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if (valida_campo_data(data_nasc.text.toString()) == false) {
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
            val inicioData = try { LocalDate.parse(ini_cartela.text.toString(), DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)) } catch (e: Exception) { null }
            if (inicioData == null || inicioData.isAfter(LocalDate.now())) {
                Toast.makeText(this, "Confira a data de início da cartela", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val inicio = inicioData.toString()
            val (ativos, placebo, pausa) = cartelas[cartela.selectedItemPosition]

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.get(this@cadastro)
                val userId = db.userDao().inserir(user_banco)

                val pilulaId = pilula?.id ?: db.pilulaDao().inserir(
                    BirthPill(
                        medName = nomeDigitado,
                        medNameSearch = normalizar(nomeDigitado),
                        activeDays = ativos,
                        placeboDays = placebo,
                        pauseDays = pausa,
                        missHours = null,
                        sourceDate = null
                    )
                )
                db.pillPackDao().inserir(
                    PillPack(
                        userId = userId,
                        birthPillId = pilulaId,
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