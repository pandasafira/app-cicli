package com.example.aplicativocicli.classes
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.aplicativocicli.data.User

@Dao
interface userDao {
    @Insert
    suspend fun inserir(usuario: User): Long
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun buscarUsuario(): User?
}

