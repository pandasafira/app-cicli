package com.example.aplicativocicli

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.aplicativocicli.data.AppDatabase
import com.example.aplicativocicli.alarm.notificacao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.provider.Settings
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.Toolbar
import android.widget.AutoCompleteTextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.aplicativocicli.alarm.alarme_agenda
import com.example.aplicativocicli.data.DoseStatus
import com.example.aplicativocicli.data.MedicationHistory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import com.example.aplicativocicli.alarm.dia_pausa
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale


class main : AppCompatActivity() {
    private val pedirNotificacao = registerForActivityResult(ActivityResultContracts.RequestPermission()) { aceitou ->
        if (!aceitou) {
            AlertDialog.Builder(this)
                .setTitle("Notificações desativadas")
                .setMessage("Sem as notificações o alarme do anticoncepcional não vai tocar. Quer ativar?")
                .setPositiveButton("Ativar") { _, _ ->
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    startActivity(intent)
                }
                .setNegativeButton("Agora não", null)
                .show()
        }
    }

    private fun verificarNotificacao() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            pedirNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= 34) {
            val nm = getSystemService(android.app.NotificationManager::class.java)
            if (!nm.canUseFullScreenIntent()) {
                startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, android.net.Uri.parse("package:$packageName")))
            }
        }
        val pm = getSystemService(android.os.PowerManager::class.java)
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, android.net.Uri.parse("package:$packageName")))
        }
    }

    private fun atualizarHorario() {
        val relogio = findViewById<TextView>(R.id.clock)
        lifecycleScope.launch(Dispatchers.IO) {
            val alarme = AppDatabase.get(this@main).alarmDao().buscarUltimo()
            withContext(Dispatchers.Main) {
                relogio.text = if (alarme != null) "%02d:%02d".format(alarme.hour, alarme.minute) else "--:--"
            }
        }
    }

    private fun nomePilula() {
        val pilula = findViewById<TextView>(R.id.texto_nome_pilula)
        lifecycleScope.launch(Dispatchers.IO) {
            val nome = AppDatabase.get(this@main).pillPackDao().buscarAtual()
            withContext(Dispatchers.Main) {
                pilula.text = nome ?: "Sem pílula cadastrada"
            }
        }
    }

    private fun atualizarBotao() {
        val botaoAlarme = findViewById<Button>(R.id.botao_alarme_teste)
        val botaoTomei = findViewById<Button>(R.id.botao_tomei)
        botaoAlarme.setOnClickListener { startActivity(Intent(this, alarme::class.java)) }
        botaoTomei.setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar pílula")
                .setMessage("Você tomou sua pílula de hoje?")
                .setNegativeButton("Ainda não", null)
                .setPositiveButton("Sim, tomei") { _, _ -> confirmarTomada() }
                .show()
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.get(this@main)
            val alarme = db.alarmDao().buscarUltimo()
            val cartela = db.pillPackDao().cartelaAtual()
            var texto = "Tomei minha pílula"
            var habilitado = true
            if (alarme != null && cartela?.startDate != null) {
                val pausa = dia_pausa.e_pausa(LocalDate.parse(cartela.startDate), cartela.activeDays, cartela.placeboDays, cartela.pauseDays)
                val dose = db.historicoDao().buscarDoseDoDia(cartela.id, alarme.id, LocalDate.now().toString())
                if (pausa) {
                    texto = "Hoje é dia de pausa"
                    habilitado = false
                } else if (dose?.status == DoseStatus.TAKEN) {
                    texto = "Pílula de hoje tomada"
                    habilitado = false
                }
            }
            withContext(Dispatchers.Main) {
                botaoAlarme.visibility = if (alarme != null) View.GONE else View.VISIBLE
                botaoTomei.visibility = if (alarme != null) View.VISIBLE else View.GONE
                botaoTomei.text = texto
                botaoTomei.isEnabled = habilitado
            }
        }
    }

    private fun confirmarTomada() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.get(this@main)
            val alarme = db.alarmDao().buscarUltimo() ?: return@launch
            val cartela = db.pillPackDao().cartelaAtual() ?: return@launch
            val usuario = db.userDao().buscarUsuario() ?: return@launch
            val agora = LocalDateTime.now().withNano(0)
            val horario = LocalDateTime.of(LocalDate.now(), LocalTime.of(alarme.hour, alarme.minute))
            val id = alarme.id.toInt()

            db.historicoDao().inserir(
                MedicationHistory(
                    userId = usuario.id,
                    packId = cartela.id,
                    alarmId = alarme.id,
                    scheduledAt = horario.toString(),
                    confirmedAt = agora.toString(),
                    status = DoseStatus.TAKEN,
                    createdAt = agora.toString()
                )
            )

            alarme_agenda.cancelarRepeticao(this@main, id)
            notificacao.cancelar(this@main, id)
            if (agora.isBefore(horario)) {
                alarme_agenda.cancel(this@main, id)
                alarme_agenda.agendar(this@main, alarme, pularHoje = true)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@main, "Pílula confirmada!", Toast.LENGTH_SHORT).show()
                atualizarBotao()
            }
        }
    }

    private fun atualizar_urso(){
        val urso_manha = findViewById<ImageView>(R.id.urso_manha)
        val urso_noite = findViewById<ImageView>(R.id.urso_noite)

        lifecycleScope.launch(Dispatchers.IO) {
            val agora = LocalTime.now()
            val formatador = DateTimeFormatter.ofPattern("HH:mm")
            val horaAtual = agora.format(formatador)
            withContext(Dispatchers.Main) {
                if (horaAtual > "19:00" || horaAtual < "06:00" ) {
                    urso_manha.visibility = View.GONE
                    urso_noite.visibility = View.VISIBLE
                } else {
                    urso_manha.visibility = View.VISIBLE
                    urso_noite.visibility = View.GONE
                }
            }
        }
    }

    private fun atualizarCalendario() {
        val calendario = findViewById<LinearLayout>(R.id.card_calendario)
        val hoje = LocalDate.now()
        val ptBR = Locale.forLanguageTag("pt-BR")
        val mesAno = findViewById<TextView>(R.id.texto_mes_ano)
        mesAno.text = hoje.format(DateTimeFormatter.ofPattern("MMMM yyyy", ptBR)).uppercase()
        for (i in 0 until calendario.childCount) {
            val dia = hoje.plusDays((i - 3).toLong())
            val nomeDia = dia.dayOfWeek.getDisplayName(TextStyle.SHORT, ptBR).uppercase().replace(".", "")
            val texto = calendario.getChildAt(i) as TextView
            texto.text = "${dia.dayOfMonth}\n$nomeDia"
            if (dia == hoje) {
                texto.setTextColor(Color.parseColor("#B86D82"))
                texto.setTypeface(texto.typeface, Typeface.BOLD)
            } else {
                texto.setTextColor(Color.parseColor("#8A7A7D"))
                texto.setTypeface(Typeface.create(texto.typeface, Typeface.NORMAL))
            }
        }
    }

    private fun atualizarPeriodo() {
        val texto = findViewById<TextView>(R.id.texto_periodo)
        lifecycleScope.launch(Dispatchers.IO) {
            val cartela = AppDatabase.get(this@main).pillPackDao().cartelaAtual()
            withContext(Dispatchers.Main) {
                if (cartela?.startDate == null) {
                    texto.text = ""
                    return@withContext
                }
                val ativos = cartela.activeDays
                val placebo = cartela.placeboDays
                val pausa = cartela.pauseDays
                val ciclo = ativos + placebo + pausa
                val dias = ChronoUnit.DAYS.between(LocalDate.parse(cartela.startDate), LocalDate.now())
                val dia = Math.floorMod(dias, ciclo.toLong()).toInt() + 1
                texto.text = when {
                    placebo + pausa == 0 -> "Uso contínuo"
                    pausa > 0 && dia == ciclo -> "Amanhã começa a nova cartela"
                    dia > ativos + placebo -> "Pausa da cartela"
                    dia > ativos -> "Pílulas de placebo"
                    dia > ativos - 3 -> "Reta final da cartela"
                    dia <= 7 -> "Nova cartela 🌸"
                    else -> "Fase ativa"
                }
            }
        }
    }

    private fun atualizarComprimido() {
        val texto = findViewById<TextView>(R.id.texto_comprimido)
        lifecycleScope.launch(Dispatchers.IO) {
            val cartela = AppDatabase.get(this@main).pillPackDao().cartelaAtual()
            withContext(Dispatchers.Main) {
                if (cartela?.startDate == null) {
                    texto.text = ""
                    return@withContext
                }
                val ativos = cartela.activeDays
                val placebo = cartela.placeboDays
                val ciclo = ativos + placebo + cartela.pauseDays
                val dias = ChronoUnit.DAYS.between(LocalDate.parse(cartela.startDate), LocalDate.now())
                val dia = Math.floorMod(dias, ciclo.toLong()).toInt() + 1
                texto.text = when {
                    dia > ativos + placebo -> "Pausa: dia ${dia - ativos - placebo} de ${cartela.pauseDays}"
                    dia > ativos -> "Placebo ${dia - ativos} de $placebo"
                    else -> "Comprimido $dia de $ativos"
                }
            }
        }
    }

    private fun atualizarAviso() {
        val aviso = findViewById<View>(R.id.card_aviso)
        val prefs = getSharedPreferences("cicli", MODE_PRIVATE)
        aviso.visibility = if (prefs.getBoolean("aviso_troca", false)) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.botao_entendi_aviso).setOnClickListener {
            prefs.edit().putBoolean("aviso_troca", false).apply()
            aviso.visibility = View.GONE
        }
    }


    override fun onResume() {
        super.onResume()
        atualizarHorario()
        nomePilula()
        atualizarBotao()
        atualizar_urso()
        atualizarCalendario()
        atualizarPeriodo()
        atualizarComprimido()
        atualizarAviso()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("cicli", MODE_PRIVATE)
        if (!prefs.getBoolean("cadastrado", false)) {
            startActivity(Intent(this, cadastro::class.java))
            finish()
            return
        }
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val busca = findViewById<AutoCompleteTextView>(R.id.busca)
        lifecycleScope.launch(Dispatchers.IO) {
            val pilulas = AppDatabase.get(this@main).pilulaDao().listar()
            withContext(Dispatchers.Main) {
                busca.setAdapter(ArrayAdapter(this@main, android.R.layout.simple_dropdown_item_1line, pilulas.map { it.medName }))
                busca.setOnItemClickListener { parent, _, position, _ ->
                    val nome = parent.getItemAtPosition(position) as String
                    val pilula = pilulas.first { it.medName == nome }
                    busca.setText("")
                    startActivity(Intent(this@main, infoPilula::class.java).putExtra("pill_id", pilula.id))
                }
            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val nav = findViewById<NavigationView>(R.id.nav_lateral)

        toolbar.setNavigationOnClickListener { drawer.openDrawer(GravityCompat.START) }

        nav.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_alarme -> startActivity(Intent(this, config_alarme::class.java))
                R.id.menu_cartela -> startActivity(Intent(this, alterar_cartela::class.java))
            }
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        verificarNotificacao()

        fun config_alarme() {
            startActivity(Intent(this, alarme::class.java))
        }

        fun historico() {
            startActivity(Intent(this, historicoAlarme::class.java))
        }
        val hist = findViewById<View>(R.id.card_historico)

        hist.setOnClickListener { historico() }

        lifecycleScope.launch(Dispatchers.IO) {
            val usuario = AppDatabase.get(this@main).userDao().buscarUsuario()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@main, "Olá, ${usuario?.name}", Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch(Dispatchers.IO){
            val cartela = AppDatabase.get(this@main).pillPackDao().cartelaAtual()
            withContext(Dispatchers.Main) {
                val texto = findViewById<TextView>(R.id.texto_comprimido)
                if (cartela?.startDate != null) {
                    val ciclo = cartela.activeDays + cartela.placeboDays + cartela.pauseDays
                    val dias = ChronoUnit.DAYS.between(LocalDate.parse(cartela.startDate), LocalDate.now())
                    val dia = Math.floorMod(dias, ciclo.toLong()).toInt() + 1
                    val ativos = cartela.activeDays
                    val placebo = cartela.placeboDays
                    texto.text = when {
                        dia > ativos + placebo -> "Pausa: dia ${dia - ativos - placebo} de ${cartela.pauseDays}"
                        dia > ativos -> "Placebo ${dia - ativos} de $placebo"
                        else -> "Comprimido $dia de $ativos"
                    }
                } else {
                    texto.text = ""
                }
            }
        }


        notificacao.createChannel(this)
        }
    }
