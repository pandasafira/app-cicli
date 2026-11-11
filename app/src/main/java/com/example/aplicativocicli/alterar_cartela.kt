package com.example.aplicativocicli

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.aplicativocicli.alarm.alarme_agenda
import com.example.aplicativocicli.data.AppDatabase
import com.example.aplicativocicli.data.BirthPill
import com.example.aplicativocicli.data.PillPack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class alterar_cartela : AppCompatActivity() {
    private val cartelas = listOf(
        Triple(21, 0, 7),
        Triple(22, 0, 6),
        Triple(24, 0, 4),
        Triple(28, 0, 2),
        Triple(21, 7, 0),
        Triple(24, 4, 0),
        Triple(84, 0, 7),
        Triple(28, 0, 0)
    )
    private val formato = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.alterar_cartela)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(0, 0, 0, teclado.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { finish() }

        val editNome = findViewById<AutoCompleteTextView>(R.id.edit_novo_anticoncepcional)
        val spinner = findViewById<Spinner>(R.id.spinner_nova_cartela)
        val editInicio = findViewById<EditText>(R.id.edit_inicio_nova_cartela)
        val botao = findViewById<Button>(R.id.botao_salvar_cartela)

        mascaraData(editInicio)
        mostrarAtual()

        botao.setOnClickListener {
            val nomeDigitado = editNome.text.toString().trim()
            val txtErroNome = findViewById<TextView>(R.id.txt_erro_nome)

            if (nomeDigitado.isEmpty()) {
                txtErroNome.visibility = View.VISIBLE
                editNome.requestFocus()
                return@setOnClickListener
            } else {
                txtErroNome.visibility = View.GONE
            }

            val inicio = try {
                LocalDate.parse(editInicio.text.toString().trim(), formato)
            } catch (e: Exception) {
                null
            }
            if (inicio == null || inicio.isAfter(LocalDate.now())) {
                Toast.makeText(this, "Confira a data de início", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val (ativos, placebo, pausa) = cartelas[spinner.selectedItemPosition]

            MaterialAlertDialogBuilder(this)
                .setTitle("Alterar cartela")
                .setMessage("Trocar para: ${spinner.selectedItem}\nInício: ${editInicio.text}\n\nA cartela atual continua salva no histórico.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Alterar") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.get(this@alterar_cartela)
                    val usuario = db.userDao().buscarUsuario() ?: return@launch
                    val atual = db.pillPackDao().cartelaAtual()

                    val pilulaId: Long? = if (nomeDigitado.isBlank()) {
                        atual?.birthPillId
                    } else {
                        val existente = db.pilulaDao().listar().find { normalizar(it.medName) == normalizar(nomeDigitado) }
                        existente?.id ?: db.pilulaDao().inserir(
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
                    }

                    db.pillPackDao().desmarcarAtuais(usuario.id)
                    db.pillPackDao().inserir(
                        PillPack(
                            userId = usuario.id,
                            birthPillId = pilulaId,
                            activeDays = ativos,
                            placeboDays = placebo,
                            pauseDays = pausa,
                            startDate = inicio.toString(),
                            isCurrent = true,
                            createdAt = LocalDate.now().toString()
                        )
                    )
                    getSharedPreferences("cicli", MODE_PRIVATE).edit().putBoolean("aviso_troca", true).apply()

                    // realinha os alarmes ativos com a nova cartela
                    db.alarmDao().listar().filter { it.active }.forEach {
                        alarme_agenda.cancel(this@alterar_cartela, it.id.toInt())
                        alarme_agenda.cancelarRepeticao(this@alterar_cartela, it.id.toInt())
                        alarme_agenda.agendar(this@alterar_cartela, it)
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@alterar_cartela, "Cartela atualizada!", Toast.LENGTH_SHORT).show()
                        finish()
                        }
                    }
                }.show()
            }
        }

    private fun mostrarAtual() {
        val txtPilula = findViewById<TextView>(R.id.txt_pilula_atual)
        val txtTipo = findViewById<TextView>(R.id.txt_tipo_atual)
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.get(this@alterar_cartela)
            val nome = db.pillPackDao().buscarAtual()
            val cartela = db.pillPackDao().cartelaAtual()
            withContext(Dispatchers.Main) {
                txtPilula.text = nome ?: "Sem pílula cadastrada"
                txtTipo.text = if (cartela == null) "" else {
                    val partes = mutableListOf("${cartela.activeDays} ativos")
                    if (cartela.placeboDays > 0) partes.add("${cartela.placeboDays} placebo")
                    if (cartela.pauseDays > 0) partes.add("${cartela.pauseDays} de pausa")
                    partes.joinToString(" + ")
                }
            }
            val pilulas = AppDatabase.get(this@alterar_cartela).pilulaDao().listar()
            withContext(Dispatchers.Main) {
                val editNome = findViewById<AutoCompleteTextView>(R.id.edit_novo_anticoncepcional)
                editNome.setAdapter(
                    ArrayAdapter(
                        this@alterar_cartela,
                        android.R.layout.simple_dropdown_item_1line,
                        pilulas.map { it.medName })
                )
            }
        }
    }

    private fun normalizar(texto: String) =
        Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")

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
}