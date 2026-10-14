package com.example.aplicativocicli

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.Toast
import com.example.projetoappcicli.R



class MainActivity : AppCompatActivity() {
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

        val botao_alarme = findViewById<Button>(R.id.botao_alarme_teste)

        botao_alarme.setOnClickListener {
            botao_alarme.visibility = View.INVISIBLE
            Toast.makeText(this, "Funcionou!", Toast.LENGTH_SHORT).show()
            }
        }
    }
