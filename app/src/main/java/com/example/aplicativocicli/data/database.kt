package com.example.aplicativocicli.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aplicativocicli.classes.alarmDao
import com.example.aplicativocicli.classes.userDao
import com.example.aplicativocicli.classes.pilulaDao
import com.example.aplicativocicli.classes.pillPack.pillPackDao
import com.example.aplicativocicli.classes.historicoDao

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
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): userDao
    abstract fun alarmDao(): alarmDao
    abstract fun pilulaDao(): pilulaDao
    abstract fun pillPackDao() : pillPackDao
    abstract fun historicoDao() : historicoDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alarm ADD COLUMN sound_index INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE birth_pill ADD COLUMN source TEXT NOT NULL DEFAULT 'anvisa'")
            }
        }

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cicli.db"
                )
                    .createFromAsset("cicli.db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
    }
}
