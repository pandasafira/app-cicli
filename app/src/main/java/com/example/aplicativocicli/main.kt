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
import com.example.aplicativocicli.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class main : AppCompatActivity() {
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

        // so para testar banco
        lifecycleScope.launch(Dispatchers.IO) {
            val usuario = AppDatabase.get(this@main).userDao().buscarUsuario()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@main, "Olá, ${usuario?.name}", Toast.LENGTH_SHORT).show()
            }
        }

        fun config_alarme() {
            startActivity(Intent(this, alarme::class.java))
            finish()
        }

        val botao_alarme = findViewById<Button>(R.id.botao_alarme_teste)

        botao_alarme.setOnClickListener {
            config_alarme()
            }
        }
    }
