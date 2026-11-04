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
import java.time.LocalDate
import java.time.temporal.ChronoUnit


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
        val botao_alarme1 = findViewById<Button>(R.id.botao_alarme_teste)
        val botao_alarme2_tomei = findViewById<Button>(R.id.botao_tomei)
        botao_alarme1.setOnClickListener { config_alarme() }
        lifecycleScope.launch(Dispatchers.IO) {
            val tem_alarme = AppDatabase.get(this@main).alarmDao().buscarAtivo()
            withContext(Dispatchers.Main) {
                if (tem_alarme != null) {
                    botao_alarme1.visibility = View.GONE
                    botao_alarme2_tomei.visibility = View.VISIBLE
                } else {
                    botao_alarme1.visibility = View.VISIBLE
                    botao_alarme2_tomei.visibility = View.GONE
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        atualizarHorario()
        nomePilula()
        atualizarBotao()
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

        // so para testar banco
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
                    val extra = cartela.placeboDays + cartela.pauseDays
                    texto.text = if (dia > cartela.activeDays + cartela.placeboDays) "Pausa da cartela"
                    else "Comprimido $dia de ${cartela.activeDays}+$extra"
                } else {
                    texto.text = ""
                }
            }
        }


        notificacao.createChannel(this)
        }
    }
