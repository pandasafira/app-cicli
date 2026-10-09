package com.example.aplicativocicli.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        BirthPill::class,
        ActiveIngredient::class,
        BirthPillIngredient::class,
        MedicineToAvoid::class,
        BirthPillMedicineToAvoid::class,
        PillPack::class,
        Alarm::class,
        MedicationHistory::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cicli.db"
                )
                    .createFromAsset("cicli.db")
                    .build()
                    .also { instance = it }
            }
    }
}