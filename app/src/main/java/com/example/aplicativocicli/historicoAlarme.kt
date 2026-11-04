package com.example.aplicativocicli

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.example.aplicativocicli.data.AppDatabase
import com.example.aplicativocicli.data.DoseStatus
import com.example.aplicativocicli.data.MedicationHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class historicoAlarme : AppCompatActivity() {
    private val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
    private val formatoMes = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.hist_alarme)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        carregar()
    }

    private fun carregar() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.get(this@historicoAlarme).historicoDao()
            dao.limparAntigos(LocalDate.now().minusMonths(3).toString())
            val registros = dao.listar()
            withContext(Dispatchers.Main) {
                val lista = findViewById<LinearLayout>(R.id.lista_historico)
                lista.removeAllViews()
                registros.groupBy { LocalDateTime.parse(it.scheduledAt).toLocalDate().withDayOfMonth(1) }
                    .forEach { (mes, itens) ->
                        adicionarTitulo(lista, mes.format(formatoMes).uppercase())
                        itens.forEach { adicionarCard(lista, it) }
                    }
            }
        }
    }

    private fun adicionarTitulo(lista: LinearLayout, texto: String) {
        val dp = resources.displayMetrics.density
        val titulo = TextView(this).apply {
            text = texto
            textSize = 14f
            setTextColor(0xFF5A2E3A.toInt())
            typeface = ResourcesCompat.getFont(this@historicoAlarme, R.font.silkscreen_bold)
            setPadding(0, (16 * dp).toInt(), 0, (8 * dp).toInt())
        }
        lista.addView(titulo)
    }

    private fun adicionarCard(lista: LinearLayout, r: MedicationHistory) {
        val card = layoutInflater.inflate(R.layout.card_hist, lista, false)
        val tomada = r.status == DoseStatus.TAKEN
        val data = LocalDateTime.parse(r.confirmedAt ?: r.scheduledAt).format(formato)
        val icone = card.findViewById<TextView>(R.id.icone_status)

        card.findViewById<TextView>(R.id.txt_status).text = if (tomada) "PÍLULA TOMADA" else "NÃO TOMADA"
        card.findViewById<TextView>(R.id.txt_data).text = data
        icone.text = if (tomada) "✓" else "✕"
        icone.setTextColor(if (tomada) 0xFF5E9A5A.toInt() else 0xFFA33A3A.toInt())
        card.findViewById<View>(R.id.aviso).visibility = if (tomada) View.GONE else View.VISIBLE
        lista.addView(card)
    }
}