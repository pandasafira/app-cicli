package com.example.aplicativocicli.classes

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aplicativocicli.data.DoseStatus
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

    @Query("SELECT COUNT(*) FROM medication_history WHERE alarm_id = :alarmId AND scheduled_at LIKE :dia || '%'")
    suspend fun contarNoDia(alarmId: Long, dia: String): Int

    @Query("SELECT COUNT(*) FROM medication_history WHERE alarm_id = :alarmId AND substr(scheduled_at, 1, 10) = :dia")
    suspend fun registrosNoDia(alarmId: Long, dia: String): Int

    @Query("SELECT * FROM medication_history WHERE pack_id = :packId AND alarm_id = :alarmId AND substr(scheduled_at, 1, 10) = :dia LIMIT 1")
    suspend fun buscarDoseDoDia(packId: Long, alarmId: Long, dia: String): MedicationHistory
    data class HistoricoItem(
        val id: Long,
        @ColumnInfo(name = "scheduled_at") val scheduledAt: String,
        @ColumnInfo(name = "confirmed_at") val confirmedAt: String?,
        val status: DoseStatus,
        @ColumnInfo(name = "pill_name") val pillName: String
    )

    @Query("SELECT h.id, h.scheduled_at, h.confirmed_at, h.status, COALESCE(bp.med_name, 'Anticoncepcional') AS pill_name FROM medication_history h JOIN pill_pack pp ON pp.id = h.pack_id LEFT JOIN birth_pill bp ON bp.id = pp.birth_pill_id ORDER BY h.scheduled_at DESC")
    suspend fun listarHist(): List<HistoricoItem>
}