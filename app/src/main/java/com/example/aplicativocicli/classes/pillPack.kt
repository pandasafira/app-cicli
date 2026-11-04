package com.example.aplicativocicli.classes
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aplicativocicli.data.PillPack

class pillPack {
    @Dao
    interface pillPackDao {
        @Insert
        suspend fun inserir(cartela: PillPack): Long
        @Query("SELECT bp.med_name FROM pill_pack pp join birth_pill bp on bp.id = pp.birth_pill_id WHERE pp.is_current = 1 ORDER BY pp.id DESC LIMIT 1 ")
        suspend fun buscarAtual(): String?

        @Query("SELECT * FROM pill_pack WHERE is_current = 1 ORDER BY id DESC LIMIT 1")
        suspend fun cartelaAtual(): PillPack?
    }
}