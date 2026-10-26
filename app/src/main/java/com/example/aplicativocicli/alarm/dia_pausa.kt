package com.example.aplicativocicli.alarm

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object dia_pausa {
    fun e_pausa(startDate: LocalDate, active: Int, placebo: Int, pause: Int, today: LocalDate = LocalDate.now()): Boolean {
        val cycle = active + placebo + pause
        val day = (ChronoUnit.DAYS.between(startDate, today) % cycle).toInt()
        return day >= active + placebo
    }
}