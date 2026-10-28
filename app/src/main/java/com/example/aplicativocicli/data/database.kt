package com.example.aplicativocicli.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.aplicativocicli.classes.alarmDao
import com.example.aplicativocicli.classes.userDao
import com.example.aplicativocicli.classes.pilulaDao
import com.example.aplicativocicli.classes.pillPack.pillPackDao

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
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): userDao
    abstract fun alarmDao(): alarmDao
    abstract fun pilulaDao(): pilulaDao
    abstract fun pillPackDao() : pillPackDao

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
