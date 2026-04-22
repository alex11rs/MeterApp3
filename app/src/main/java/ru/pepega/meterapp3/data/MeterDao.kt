package ru.pepega.meterapp3.data

import kotlinx.coroutines.flow.Flow
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MeterDao {
    @Query("SELECT COUNT(*) FROM apartments")
    fun getApartmentCount(): Int

    @Query("SELECT * FROM apartments ORDER BY name COLLATE NOCASE ASC")
    fun getApartments(): List<ApartmentEntity>

    @Query("SELECT * FROM apartments ORDER BY name COLLATE NOCASE ASC")
    fun observeApartments(): Flow<List<ApartmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertApartments(apartments: List<ApartmentEntity>)

    @Query("DELETE FROM apartments")
    fun deleteAllApartments()

    @Query("SELECT COUNT(*) FROM meter_configs")
    fun getMeterConfigCount(): Int

    @Query("SELECT * FROM meter_configs ORDER BY apartmentId ASC, displayOrder ASC, name COLLATE NOCASE ASC")
    fun getAllMeterConfigs(): List<MeterConfigEntity>

    @Query("SELECT * FROM meter_configs WHERE apartmentId = :apartmentId ORDER BY displayOrder ASC, name COLLATE NOCASE ASC")
    fun getMeterConfigs(apartmentId: String): List<MeterConfigEntity>

    @Query("SELECT * FROM meter_configs WHERE apartmentId = :apartmentId ORDER BY displayOrder ASC, name COLLATE NOCASE ASC")
    fun observeMeterConfigs(apartmentId: String): Flow<List<MeterConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMeterConfigs(configs: List<MeterConfigEntity>)

    @Update
    fun updateMeterConfig(config: MeterConfigEntity)

    @Query("SELECT * FROM meter_configs WHERE id = :id LIMIT 1")
    fun getMeterConfigById(id: String): MeterConfigEntity?

    @Query("DELETE FROM meter_configs WHERE id = :id")
    fun deleteMeterConfigById(id: String)

    @Query("DELETE FROM meter_configs")
    fun deleteAllMeterConfigs()

    @Query("DELETE FROM meter_configs WHERE apartmentId = :apartmentId")
    fun deleteMeterConfigsByApartment(apartmentId: String)

    @Query("SELECT * FROM meter_data WHERE meterId = :meterId")
    fun getMeterData(meterId: String): MeterDataEntity?

    @Query("SELECT * FROM meter_data WHERE meterId = :meterId")
    fun observeMeterData(meterId: String): Flow<MeterDataEntity?>

    @Query("SELECT * FROM meter_data")
    fun observeAllMeterData(): Flow<List<MeterDataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMeterData(data: MeterDataEntity)

    @Query("DELETE FROM meter_data WHERE meterId = :meterId")
    fun deleteMeterData(meterId: String)

    @Query("DELETE FROM meter_data")
    fun deleteAllMeterData()

    @Query("SELECT * FROM meter_history WHERE meterId = :meterId ORDER BY timestamp ASC, id ASC")
    fun getMeterHistory(meterId: String): List<MeterHistoryEntity>

    @Query("SELECT * FROM meter_history WHERE meterId = :meterId ORDER BY timestamp ASC, id ASC")
    fun observeMeterHistory(meterId: String): Flow<List<MeterHistoryEntity>>

    @Query("SELECT * FROM meter_history ORDER BY meterId ASC, timestamp ASC, id ASC")
    fun observeAllMeterHistory(): Flow<List<MeterHistoryEntity>>

    @Query("""
        SELECT * FROM meter_history m1
        WHERE (
            SELECT COUNT(*) FROM meter_history m2
            WHERE m2.meterId = m1.meterId
            AND (m2.timestamp > m1.timestamp OR (m2.timestamp = m1.timestamp AND m2.id > m1.id))
        ) < 2
        ORDER BY meterId ASC, timestamp ASC, id ASC
    """)
    fun observeLastTwoMeterHistory(): Flow<List<MeterHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMeterHistory(history: List<MeterHistoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMeterHistoryEntry(entry: MeterHistoryEntity)

    @Query("DELETE FROM meter_history WHERE id = :id")
    fun deleteMeterHistoryEntryById(id: Long)

    @Query("DELETE FROM meter_history WHERE meterId = :meterId")
    fun deleteMeterHistory(meterId: String)

    @Query("DELETE FROM meter_history")
    fun deleteAllMeterHistory()
}
