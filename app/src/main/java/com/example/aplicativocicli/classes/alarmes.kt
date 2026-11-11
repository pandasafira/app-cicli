package com.example.aplicativocicli.classes
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.aplicativocicli.data.Alarm


@Dao
interface alarmDao {
    @Insert
    suspend fun inserir(alarme: Alarm): Long
    @Query("SELECT * FROM alarm WHERE active = 1 LIMIT 1")
    suspend fun buscarAtivo(): Alarm?

    @Query("SELECT * FROM alarm WHERE active = 1 ORDER BY id DESC LIMIT 1")
    suspend fun buscarUltimo(): Alarm?

    @Query("SELECT * FROM alarm ORDER BY hour, minute")
    suspend fun listar(): List<Alarm>
    @Delete
    suspend fun excluir(alarme: Alarm)

    @Query("SELECT * FROM alarm WHERE id = :id")
    suspend fun buscarPorId(id: Long): Alarm?

    @Update
    suspend fun atualizar(alarme: Alarm)

}
