package com.example.aplicativocicli.classes
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aplicativocicli.data.Alarm


@Dao
interface alarmDao {
    @Insert
    suspend fun inserir(alarme: Alarm): Long
    @Query("SELECT * FROM alarm WHERE active = 1 LIMIT 1")
    suspend fun buscarAtivo(): Alarm?
}