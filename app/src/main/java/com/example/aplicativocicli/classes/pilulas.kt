package com.example.aplicativocicli.classes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aplicativocicli.data.BirthPill

@Dao
interface pilulaDao {
    @Query("SELECT * FROM birth_pill ORDER BY med_name")
    suspend fun listar(): List<BirthPill>

    @Insert
    suspend fun inserir(pilula: BirthPill): Long

    @Query("SELECT * FROM birth_pill WHERE id = :id")
    suspend fun buscarPorId(id: Long): BirthPill?

    @Query("SELECT ai.name FROM active_ingredient ai JOIN birth_pill_ingredient bpi ON bpi.ingredient_id = ai.id WHERE bpi.birth_pill_id = :id")
    suspend fun principiosAtivos(id: Long): List<String>

    @Query("SELECT mta.* FROM medicine_to_avoid mta JOIN birth_pill_medicine_to_avoid bpm ON bpm.medicine_to_avoid_id = mta.id WHERE bpm.birth_pill_id = :id ORDER BY mta.name")
    suspend fun medicamentosEvitar(id: Long): List<MedicineToAvoid>
}
