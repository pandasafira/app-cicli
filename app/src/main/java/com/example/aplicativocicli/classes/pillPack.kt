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
        @Query("SELECT * FROM pill_pack WHERE is_current = 1")
        suspend fun buscarAtual(): PillPack?
    }
}