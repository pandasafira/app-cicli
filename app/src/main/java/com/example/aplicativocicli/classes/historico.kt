package com.example.aplicativocicli.classes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aplicativocicli.data.MedicationHistory


@Dao
interface historicoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(registro: MedicationHistory): Long

    @Query("SELECT * FROM medication_history ORDER BY scheduled_at DESC")
    suspend fun listar(): List<MedicationHistory>

    @Query("DELETE FROM medication_history WHERE scheduled_at < :limite")
    suspend fun limparAntigos(limite: String)

    @Query("SELECT * FROM medication_history WHERE pack_id = :packId AND scheduled_at = :agendado LIMIT 1")
    suspend fun buscarDose(packId: Long, agendado: String): MedicationHistory?
}