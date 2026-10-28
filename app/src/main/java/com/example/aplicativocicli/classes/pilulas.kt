package com.example.aplicativocicli.classes

import androidx.room.Dao
import androidx.room.Query
import com.example.aplicativocicli.data.BirthPill

@Dao
interface pilulaDao {
    @Query("SELECT * FROM birth_pill ORDER BY med_name")
    suspend fun listar(): List<BirthPill>
}