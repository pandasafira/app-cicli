package com.example.aplicativocicli.classes
import androidx.room.Dao
import androidx.room.Insert
import com.example.aplicativocicli.data.User

@Dao
interface userDao {
    @Insert
    suspend fun inserir(usuario: User): Long
}