package com.example.aplicativocicli.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
enum class DoseStatus { True, False }
//user
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "birth_date") val birthDate: String,
    @ColumnInfo(name = "created_at") val createdAt: String
)

// pilulas
@Entity(
    tableName = "birth_pill",
    indices = [Index(value = ["med_name_search"])]
)
data class BirthPill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "med_name") val medName: String,
    @ColumnInfo(name = "med_name_search") val medNameSearch: String,
    @ColumnInfo(name = "active_days") val activeDays: Int,
    @ColumnInfo(name = "placebo_days") val placeboDays: Int,
    @ColumnInfo(name = "pause_days") val pauseDays: Int,
    @ColumnInfo(name = "miss_hours") val missHours: Int?,
    @ColumnInfo(name = "source_date") val sourceDate: String?
)

// composicao da pilula, principio ativo
@Entity(
    tableName = "active_ingredient",
    indices = [Index(value = ["name_search"])]
)
data class ActiveIngredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "name_search") val nameSearch: String
)


// tabela relacao n:n pra conectar principio ativo com a pilula
@Entity(
    tableName = "birth_pill_ingredient",
    primaryKeys = ["birth_pill_id", "ingredient_id"],
    foreignKeys = [
        ForeignKey(
            entity = BirthPill::class,
            parentColumns = ["id"],
            childColumns = ["birth_pill_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ActiveIngredient::class,
            parentColumns = ["id"],
            childColumns = ["ingredient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["ingredient_id"])]
)
data class BirthPillIngredient(
    @ColumnInfo(name = "birth_pill_id") val birthPillId: Long,
    @ColumnInfo(name = "ingredient_id") val ingredientId: Long
)


// medicamentos pra evitar pois corta o efeito, sera usado na busca
@Entity(
    tableName = "medicine_to_avoid",
    indices = [Index(value = ["name_search"])]
)
data class MedicineToAvoid(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "name_search") val nameSearch: String,
    val why: String
)


// tablea relacoa n:n pra conectar
@Entity(
    tableName = "birth_pill_medicine_to_avoid",
    primaryKeys = ["birth_pill_id", "medicine_to_avoid_id"],
    foreignKeys = [
        ForeignKey(
            entity = BirthPill::class,
            parentColumns = ["id"],
            childColumns = ["birth_pill_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MedicineToAvoid::class,
            parentColumns = ["id"],
            childColumns = ["medicine_to_avoid_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["medicine_to_avoid_id"])]
)
data class BirthPillMedicineToAvoid(
    @ColumnInfo(name = "birth_pill_id") val birthPillId: Long,
    @ColumnInfo(name = "medicine_to_avoid_id") val medicineToAvoidId: Long
)

// historico do user com a pilula
@Entity(
    tableName = "pill_pack",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BirthPill::class,
            parentColumns = ["id"],
            childColumns = ["birth_pill_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["birth_pill_id"])
    ]
)
data class PillPack(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "birth_pill_id") val birthPillId: Long?,
    @ColumnInfo(name = "active_days") val activeDays: Int,
    @ColumnInfo(name = "placebo_days") val placeboDays: Int,
    @ColumnInfo(name = "pause_days") val pauseDays: Int,
    @ColumnInfo(name = "start_date") val startDate: String?,
    @ColumnInfo(name = "is_current") val isCurrent: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: String
)


// alarme no geral
@Entity(
    tableName = "alarm",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"])]
)
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long,
    val hour: Int,
    val minute: Int,
    @ColumnInfo(name = "interval_minutes") val intervalMinutes: Int,
    @ColumnInfo(name = "repeat_limit") val repeatLimit: Int,
    val active: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: String
)

// historico de tomadas
@Entity(
    tableName = "medication_history",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PillPack::class,
            parentColumns = ["id"],
            childColumns = ["pack_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Alarm::class,
            parentColumns = ["id"],
            childColumns = ["alarm_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["pack_id"]),
        Index(value = ["alarm_id"]),
        Index(value = ["pack_id", "scheduled_at"], unique = true)
    ]
)
data class MedicationHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "pack_id") val packId: Long,
    @ColumnInfo(name = "alarm_id") val alarmId: Long?,
    @ColumnInfo(name = "scheduled_at") val scheduledAt: String,
    @ColumnInfo(name = "confirmed_at") val confirmedAt: String?,
    val status: DoseStatus,
    @ColumnInfo(name = "created_at") val createdAt: String
)