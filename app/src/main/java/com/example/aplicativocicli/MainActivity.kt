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
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val botao_alarme = findViewById<Button>(R.id.botao_alarme_teste)
        val botao_prox = findViewById<Button>(R.id.botao_concluir)

        botao_alarme.setOnClickListener {
            botao_alarme.visibility = View.INVISIBLE
            Toast.makeText(this, "Funcionou!", Toast.LENGTH_SHORT).show()
            }

        botao_prox.setOnClickListener {
            val intencao = Intent(this, activity_cadastro::class.jav)
        }
        }
    }
