package ru.pepega.meterapp3.data

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.pepega.meterapp3.Apartment
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.MeterData
import ru.pepega.meterapp3.MeterReadingHistoryEntry
import ru.pepega.meterapp3.MeterTariffType

@Entity(tableName = "apartments")
data class ApartmentEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(
    tableName = "meter_configs",
    indices = [Index(value = ["apartmentId"])]
)
data class MeterConfigEntity(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String,
    val unit: String,
    val decimalDigits: Int,
    val color: String,
    val enabled: Boolean,
    val displayOrder: Int,
    val model: String,
    val apartmentId: String,
    val tariffType: String,
    val verificationDate: Long,
    val validityYears: Int
)

@Entity(
    tableName = "meter_data",
    foreignKeys = [ForeignKey(
        entity = MeterConfigEntity::class,
        parentColumns = ["id"],
        childColumns = ["meterId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MeterDataEntity(
    @PrimaryKey val meterId: String,
    val current: Float,
    val previous: Float,
    val tariff: Float,
    val currentNight: Float,
    val previousNight: Float,
    val nightTariff: Float,
    val lastUpdate: Long
)

@Entity(
    tableName = "meter_history",
    indices = [Index(value = ["meterId"])],
    foreignKeys = [ForeignKey(
        entity = MeterConfigEntity::class,
        parentColumns = ["id"],
        childColumns = ["meterId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MeterHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val meterId: String,
    val value: Float,
    val timestamp: Long,
    val secondaryValue: Float?
)

@Database(
    entities = [
        ApartmentEntity::class,
        MeterConfigEntity::class,
        MeterDataEntity::class,
        MeterHistoryEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class MeterDatabase : RoomDatabase() {
    abstract fun meterDao(): MeterDao

    companion object {
        @Volatile
        private var instance: MeterDatabase? = null
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE meter_configs ADD COLUMN decimalDigits INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_meter_configs_apartmentId ON meter_configs(apartmentId)")

                db.execSQL("""
                    CREATE TABLE meter_data_new (
                        meterId TEXT PRIMARY KEY NOT NULL,
                        current REAL NOT NULL,
                        previous REAL NOT NULL,
                        tariff REAL NOT NULL,
                        currentNight REAL NOT NULL,
                        previousNight REAL NOT NULL,
                        nightTariff REAL NOT NULL,
                        lastUpdate INTEGER NOT NULL,
                        FOREIGN KEY(meterId) REFERENCES meter_configs(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("INSERT INTO meter_data_new SELECT * FROM meter_data")
                db.execSQL("DROP TABLE meter_data")
                db.execSQL("ALTER TABLE meter_data_new RENAME TO meter_data")

                db.execSQL("""
                    CREATE TABLE meter_history_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        meterId TEXT NOT NULL,
                        value REAL NOT NULL,
                        timestamp INTEGER NOT NULL,
                        secondaryValue REAL,
                        FOREIGN KEY(meterId) REFERENCES meter_configs(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("INSERT INTO meter_history_new SELECT * FROM meter_history")
                db.execSQL("DROP TABLE meter_history")
                db.execSQL("ALTER TABLE meter_history_new RENAME TO meter_history")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_meter_history_meterId ON meter_history(meterId)")
            }
        }

        private val foreignKeyCallback = object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                db.execSQL("PRAGMA foreign_keys=ON")
            }
        }

        fun getInstance(context: Context): MeterDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MeterDatabase::class.java,
                    "meter_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(foreignKeyCallback)
                    .build()
                    .also { instance = it }
            }
        }
    }
}

fun ApartmentEntity.toModel(): Apartment = Apartment(
    id = id,
    name = name
)

fun Apartment.toEntity(): ApartmentEntity = ApartmentEntity(
    id = id,
    name = name
)

fun MeterConfigEntity.toModel(): MeterConfig = MeterConfig(
    id = id,
    name = name,
    icon = icon,
    unit = unit,
    decimalDigits = decimalDigits,
    color = color,
    enabled = enabled,
    order = displayOrder,
    model = model,
    apartmentId = apartmentId,
    tariffType = MeterTariffType.entries.firstOrNull { it.name == tariffType } ?: MeterTariffType.SINGLE,
    verificationDate = verificationDate,
    validityYears = validityYears
)

fun MeterConfig.toEntity(): MeterConfigEntity = MeterConfigEntity(
    id = id,
    name = name,
    icon = icon,
    unit = unit,
    decimalDigits = decimalDigits.coerceIn(0, 3),
    color = color,
    enabled = enabled,
    displayOrder = order,
    model = model,
    apartmentId = apartmentId,
    tariffType = tariffType.name,
    verificationDate = verificationDate,
    validityYears = validityYears
)

fun MeterDataEntity.toModel(history: List<MeterHistoryEntity>): MeterData = MeterData(
    current = current,
    previous = previous,
    tariff = tariff,
    currentNight = currentNight,
    previousNight = previousNight,
    nightTariff = nightTariff,
    lastUpdate = lastUpdate,
    history = history.map { entry ->
        MeterReadingHistoryEntry(
            value = entry.value,
            timestamp = entry.timestamp,
            secondaryValue = entry.secondaryValue
        )
    }
)

fun MeterData.toEntity(meterId: String): MeterDataEntity = MeterDataEntity(
    meterId = meterId,
    current = current,
    previous = previous,
    tariff = tariff,
    currentNight = currentNight,
    previousNight = previousNight,
    nightTariff = nightTariff,
    lastUpdate = lastUpdate
)

fun MeterReadingHistoryEntry.toEntity(meterId: String): MeterHistoryEntity = MeterHistoryEntity(
    meterId = meterId,
    value = value,
    timestamp = timestamp,
    secondaryValue = secondaryValue
)
