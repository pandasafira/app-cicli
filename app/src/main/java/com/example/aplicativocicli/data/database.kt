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
    version = 1,          // tem que ser igual ao PRAGMA user_version do cicli.db
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // Os DAOs entram aqui depois, ex.: abstract fun alarmDao(): AlarmDao

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