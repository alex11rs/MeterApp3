package ru.pepega.meterapp3

import android.content.Context
import android.content.SharedPreferences
import ru.pepega.meterapp3.data.MeterDao
import ru.pepega.meterapp3.data.MeterDatabase
import ru.pepega.meterapp3.data.MeterHistoryEntity
import ru.pepega.meterapp3.data.toEntity
import ru.pepega.meterapp3.data.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MeterRepository(
    context: Context,
    private val prefs: SharedPreferences
) {
    private val database = MeterDatabase.getInstance(context)
    private val dao: MeterDao = database.meterDao()

    init {
        runBlocking(Dispatchers.IO) {
            migrateLegacyDataIfNeeded()
            repairMojibakeIfNeeded()
            ensureBaselineData()
        }
    }

    suspend fun getApartments(): List<Apartment> = withContext(Dispatchers.IO) {
        val apartments = dao.getApartments().map { it.toModel() }
        if (apartments.isEmpty()) {
            val defaults = listOf(defaultApartmentDefinition())
            database.runInTransaction {
                dao.insertApartments(defaults.map { it.toEntity() })
            }
            syncActiveApartmentId(defaults)
            return@withContext defaults
        }
        syncActiveApartmentId(apartments)
        apartments
    }

    fun observeApartments(): Flow<List<Apartment>> {
        return dao.observeApartments().map { entities ->
            val apartments = entities.map { it.toModel() }
            syncActiveApartmentId(apartments)
            apartments
        }
    }

    suspend fun saveApartments(apartments: List<Apartment>) = withContext(Dispatchers.IO) {
        val normalized = apartments.ifEmpty { listOf(defaultApartmentDefinition()) }
        database.runInTransaction {
            dao.deleteAllApartments()
            dao.insertApartments(normalized.map { it.toEntity() })
        }
        syncActiveApartmentId(normalized)
    }

    suspend fun getActiveApartmentId(): String = withContext(Dispatchers.IO) {
        val apartments = dao.getApartments().map { it.toModel() }
        val firstId = apartments.firstOrNull()?.id ?: return@withContext DEFAULT_APARTMENT_ID
        prefs.getString("active_apartment_id", firstId) ?: firstId
    }

    suspend fun getActiveApartment(): Apartment = withContext(Dispatchers.IO) {
        val apartments = dao.getApartments().map { it.toModel() }
        val activeId = prefs.getString("active_apartment_id", null)
        apartments.firstOrNull { it.id == activeId } ?: apartments.first()
    }

    fun setActiveApartmentId(apartmentId: String) {
        prefs.edit().putString("active_apartment_id", apartmentId).apply()
    }

    suspend fun addApartment(name: String): Apartment = withContext(Dispatchers.IO) {
        val apartment = Apartment(
            id = "apartment_${System.currentTimeMillis()}",
            name = name.trim()
        )
        val current = dao.getApartments().map { it.toModel() }
        database.runInTransaction {
            dao.deleteAllApartments()
            dao.insertApartments((current + apartment).map { it.toEntity() })
        }
        syncActiveApartmentId(current + apartment)
        ensureDefaultMetersForApartment(apartment.id)
        apartment
    }

    suspend fun renameApartment(apartmentId: String, newName: String) = withContext(Dispatchers.IO) {
        val updated = dao.getApartments().map { it.toModel() }.map { apartment ->
            if (apartment.id == apartmentId) apartment.copy(name = newName.trim()) else apartment
        }
        database.runInTransaction {
            dao.deleteAllApartments()
            dao.insertApartments(updated.map { it.toEntity() })
        }
        syncActiveApartmentId(updated)
    }

    suspend fun deleteApartment(apartmentId: String): Boolean = withContext(Dispatchers.IO) {
        val apartments = dao.getApartments().map { it.toModel() }
        if (apartments.size <= 1) return@withContext false
        val wasActive = prefs.getString("active_apartment_id", null) == apartmentId
        val remainingApartments = apartments.filterNot { it.id == apartmentId }
        val configIds = dao.getAllMeterConfigs()
            .filter { it.apartmentId == apartmentId }
            .map { it.id }

        database.runInTransaction {
            configIds.forEach { clearMeterData(it) }
            dao.deleteMeterConfigsByApartment(apartmentId)
            dao.deleteAllApartments()
            dao.insertApartments(remainingApartments.map { it.toEntity() })
        }

        if (wasActive) {
            prefs.edit().putString("active_apartment_id", remainingApartments.first().id).apply()
        } else {
            syncActiveApartmentId(remainingApartments)
        }
        true
    }

    suspend fun getAllMeterConfigs(): List<MeterConfig> = withContext(Dispatchers.IO) {
        val configs = dao.getAllMeterConfigs().map { entity ->
            entity.toModel().let { config ->
                if (config.apartmentId.isBlank()) config.copy(apartmentId = DEFAULT_APARTMENT_ID) else config
            }
        }
        if (configs.isEmpty()) {
            val defaults = defaultMeterConfigsDefinition()
            dao.insertMeterConfigs(defaults.map { it.toEntity() })
            return@withContext defaults
        }
        configs
    }

    suspend fun getMeterConfigs(apartmentId: String): List<MeterConfig> = withContext(Dispatchers.IO) {
        dao.getMeterConfigs(apartmentId).map { it.toModel() }
    }

    fun observeMeterConfigs(apartmentId: String): Flow<List<MeterConfig>> {
        return dao.observeMeterConfigs(apartmentId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun saveAllMeterConfigs(configs: List<MeterConfig>) = withContext(Dispatchers.IO) {
        database.runInTransaction {
            dao.deleteAllMeterConfigs()
            if (configs.isNotEmpty()) {
                dao.insertMeterConfigs(configs.map { it.toEntity() })
            }
        }
    }

    suspend fun updateMeterConfig(config: MeterConfig) = withContext(Dispatchers.IO) {
        dao.updateMeterConfig(config.toEntity())
    }

    suspend fun addMeterConfig(config: MeterConfig) = withContext(Dispatchers.IO) {
        dao.insertMeterConfigs(listOf(config.toEntity()))
    }

    suspend fun deleteMeterConfigById(id: String) = withContext(Dispatchers.IO) {
        val apartmentId = dao.getMeterConfigById(id)?.apartmentId
        dao.deleteMeterConfigById(id)
        if (apartmentId != null) {
            ensureDefaultMetersForApartment(apartmentId)
        }
    }

    suspend fun saveMeterConfigs(configs: List<MeterConfig>, apartmentId: String) = withContext(Dispatchers.IO) {
        val normalizedConfigs = configs.ifEmpty { defaultConfigsForApartment(apartmentId) }
        database.runInTransaction {
            dao.deleteMeterConfigsByApartment(apartmentId)
            if (normalizedConfigs.isNotEmpty()) {
                dao.insertMeterConfigs(normalizedConfigs.map { it.copy(apartmentId = apartmentId).toEntity() })
            }
        }
    }

    suspend fun getMeterData(key: String): MeterData = withContext(Dispatchers.IO) {
        val entity = dao.getMeterData(key) ?: return@withContext MeterData()
        val history = dao.getMeterHistory(key)
        entity.toModel(history)
    }

    fun observeMeterData(key: String): Flow<MeterData> {
        return combine(
            dao.observeMeterData(key),
            dao.observeMeterHistory(key)
        ) { entity, history ->
            entity?.toModel(history) ?: MeterData()
        }
    }

    fun observeAllMeterData(): Flow<Map<String, MeterData>> {
        return combine(
            dao.observeAllMeterData(),
            dao.observeLastTwoMeterHistory()
        ) { dataEntities, historyEntities ->
            val historyByMeterId = historyEntities.groupBy(MeterHistoryEntity::meterId)
            dataEntities.associate { entity ->
                entity.meterId to entity.toModel(
                    history = historyByMeterId[entity.meterId].orEmpty()
                )
            }
        }
    }

    fun observeAllMeterDataWithFullHistory(): Flow<Map<String, MeterData>> {
        return combine(
            dao.observeAllMeterData(),
            dao.observeAllMeterHistory()
        ) { dataEntities, historyEntities ->
            val historyByMeterId = historyEntities.groupBy(MeterHistoryEntity::meterId)
            dataEntities.associate { entity ->
                entity.meterId to entity.toModel(
                    history = historyByMeterId[entity.meterId].orEmpty()
                )
            }
        }
    }

    suspend fun saveMeterData(key: String, data: MeterData) = withContext(Dispatchers.IO) {
        dao.insertMeterData(data.toEntity(key))
    }

    suspend fun addHistoryEntry(key: String, entry: MeterReadingHistoryEntry) = withContext(Dispatchers.IO) {
        val monthFormat = java.text.SimpleDateFormat("MM.yyyy", java.util.Locale.getDefault())
        val entryMonth = monthFormat.format(java.util.Date(entry.timestamp))
        val lastExisting = dao.getMeterHistory(key).lastOrNull()
        if (lastExisting != null && monthFormat.format(java.util.Date(lastExisting.timestamp)) == entryMonth) {
            dao.deleteMeterHistoryEntryById(lastExisting.id)
        }
        dao.insertMeterHistoryEntry(entry.toEntity(key))
    }

    suspend fun replaceHistory(key: String, history: List<MeterReadingHistoryEntry>) = withContext(Dispatchers.IO) {
        database.runInTransaction {
            dao.deleteMeterHistory(key)
            val entities = history.map { it.toEntity(key) }
            if (entities.isNotEmpty()) {
                dao.insertMeterHistory(entities)
            }
        }
    }

    suspend fun resetMeterData(key: String) = withContext(Dispatchers.IO) {
        database.runInTransaction { clearMeterData(key) }
    }

    private fun clearMeterData(key: String) {
        dao.deleteMeterHistory(key)
        dao.deleteMeterData(key)
    }

    suspend fun getColorKey(meterKey: String): String = withContext(Dispatchers.IO) {
        dao.getAllMeterConfigs().find { it.id == meterKey }?.color ?: "blue"
    }

    private fun ensureBaselineData() {
        if (dao.getApartmentCount() == 0) {
            dao.insertApartments(listOf(defaultApartmentDefinition().toEntity()))
        }
        if (dao.getMeterConfigCount() == 0) {
            dao.insertMeterConfigs(defaultMeterConfigsDefinition().map { it.toEntity() })
        }
        syncActiveApartmentId(dao.getApartments().map { it.toModel() })
    }

    private fun repairMojibakeIfNeeded() {
        val originalApartments = dao.getApartments().map { it.toModel() }
        val originalConfigs = dao.getAllMeterConfigs().map { it.toModel() }

        val repairedApartments = originalApartments.map(::normalizeApartmentModel)
        val repairedConfigs = originalConfigs.map(::normalizeMeterConfigModel)

        val apartmentsChanged = repairedApartments != originalApartments
        val configsChanged = repairedConfigs != originalConfigs

        if (!apartmentsChanged && !configsChanged) return

        database.runInTransaction {
            if (apartmentsChanged) {
                dao.deleteAllApartments()
                dao.insertApartments(repairedApartments.map { it.toEntity() })
            }
            if (configsChanged) {
                dao.deleteAllMeterConfigs()
                dao.insertMeterConfigs(repairedConfigs.map { it.toEntity() })
            }
        }
    }

    private fun syncActiveApartmentId(apartments: List<Apartment>) {
        if (apartments.isEmpty()) return
        val activeId = prefs.getString("active_apartment_id", null)
        if (activeId == null || apartments.none { it.id == activeId }) {
            prefs.edit().putString("active_apartment_id", apartments.first().id).apply()
        }
    }

    private suspend fun ensureDefaultMetersForApartment(apartmentId: String) {
        val hasConfigs = dao.getAllMeterConfigs().any { it.apartmentId == apartmentId }
        if (hasConfigs) return
        val defaults = defaultConfigsForApartment(apartmentId)
        dao.insertMeterConfigs(defaults.map { it.toEntity() })
    }

    private fun migrateLegacyDataIfNeeded() {
        val migrationKey = "room_migrated_v1"
        val alreadyMigrated = prefs.getBoolean(migrationKey, false)
        val hasRoomData = dao.getApartmentCount() > 0 || dao.getMeterConfigCount() > 0
        if (alreadyMigrated || hasRoomData) {
            if (!alreadyMigrated) {
                prefs.edit().putBoolean(migrationKey, true).apply()
            }
            return
        }

        val apartments = legacyGetApartments()
        val configs = legacyGetAllMeterConfigs()
        val normalizedApartments = apartments.ifEmpty { listOf(defaultApartmentDefinition()) }
        val normalizedConfigs = configs.ifEmpty { defaultMeterConfigsDefinition() }

        database.runInTransaction {
            dao.deleteAllMeterHistory()
            dao.deleteAllMeterData()
            dao.deleteAllMeterConfigs()
            dao.deleteAllApartments()
            dao.insertApartments(normalizedApartments.map { it.toEntity() })
            dao.insertMeterConfigs(normalizedConfigs.map { it.toEntity() })
            normalizedConfigs.forEach { config ->
                val data = legacyGetMeterData(config.id)
                if (data != MeterData()) {
                    dao.insertMeterData(data.toEntity(config.id))
                    val history = data.history.map { it.toEntity(config.id) }
                    if (history.isNotEmpty()) {
                        dao.insertMeterHistory(history)
                    }
                }
            }
        }

        syncActiveApartmentId(normalizedApartments)
        prefs.edit().putBoolean(migrationKey, true).apply()
    }

    private fun legacyGetApartments(): List<Apartment> {
        val apartmentsJson = prefs.getString("apartments", "") ?: ""
        val apartments = if (apartmentsJson.isBlank()) {
            listOf(defaultApartmentDefinition())
        } else {
            try {
                val apartmentsArray = JSONArray(apartmentsJson)
                List(apartmentsArray.length()) { index ->
                    apartmentsArray.getJSONObject(index).toApartmentModel()
                }.filter { it.id.isNotBlank() && it.name.isNotBlank() }
            } catch (_: Exception) {
                listOf(defaultApartmentDefinition())
            }
        }
        return apartments.ifEmpty { listOf(defaultApartmentDefinition()) }
    }

    private fun legacyGetAllMeterConfigs(): List<MeterConfig> {
        val configsJson = prefs.getString("meter_configs", "") ?: ""
        val parsedConfigs = if (configsJson.isBlank()) {
            defaultMeterConfigsDefinition()
        } else {
            try {
                val configsArray = JSONArray(configsJson)
                List(configsArray.length()) { index ->
                    configsArray.getJSONObject(index).toLegacyMeterConfig()
                }
            } catch (_: Exception) {
                defaultMeterConfigsDefinition()
            }
        }

        return parsedConfigs
            .ifEmpty { defaultMeterConfigsDefinition() }
            .map { config ->
                if (config.apartmentId.isBlank()) config.copy(apartmentId = DEFAULT_APARTMENT_ID) else config
            }
    }

    private fun legacyGetMeterData(key: String): MeterData {
        val historyRaw = prefs.getString("${key}_history", "") ?: ""
        return MeterData(
            current = prefs.getFloat("${key}_current", 0f),
            previous = prefs.getFloat("${key}_previous", 0f),
            tariff = prefs.getFloat("${key}_tariff", 5.0f),
            currentNight = prefs.getFloat("${key}_current_night", 0f),
            previousNight = prefs.getFloat("${key}_previous_night", 0f),
            nightTariff = prefs.getFloat("${key}_night_tariff", 0f),
            lastUpdate = prefs.getLong("${key}_lastUpdate", 0L),
            history = parseLegacyMeterHistory(historyRaw)
        )
    }

    private fun parseMeterHistory(raw: String): List<MeterReadingHistoryEntry> {
        if (raw.isBlank()) {
            return emptyList()
        }

        return try {
            val historyArray = JSONArray(raw)
            List(historyArray.length()) { index ->
                val item = historyArray.opt(index)
                when (item) {
                    is JSONObject -> MeterReadingHistoryEntry(
                        value = item.optDouble("value").toFloat(),
                        timestamp = item.optLong("timestamp"),
                        secondaryValue = if (item.has("secondaryValue")) item.optDouble("secondaryValue").toFloat() else null
                    )
                    is Number -> MeterReadingHistoryEntry(
                        value = item.toFloat(),
                        timestamp = 0L
                    )
                    else -> MeterReadingHistoryEntry(0f, 0L)
                }
            }.takeLast(5)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun defaultApartment(): Apartment = Apartment(
        DEFAULT_APARTMENT_ID,
        "\u041E\u0441\u043D\u043E\u0432\u043D\u0430\u044F"
    )

    private fun defaultMeterConfigs(): List<MeterConfig> {
        return listOf(
            MeterConfig(id = "water_cold", name = "\u0425\u043E\u043B\u043E\u0434\u043D\u0430\u044F \u0432\u043E\u0434\u0430", icon = "\uD83D\uDCA7", unit = "\u043C\u00B3", decimalDigits = 3, color = "blue", enabled = true, model = "", apartmentId = DEFAULT_APARTMENT_ID),
            MeterConfig(id = "water_hot", name = "\u0413\u043E\u0440\u044F\u0447\u0430\u044F \u0432\u043E\u0434\u0430", icon = "\uD83D\uDD25", unit = "\u043C\u00B3", decimalDigits = 3, color = "red", enabled = true, model = "", apartmentId = DEFAULT_APARTMENT_ID),
            MeterConfig(id = "electricity", name = "\u042D\u043B\u0435\u043A\u0442\u0440\u0438\u0447\u0435\u0441\u0442\u0432\u043E", icon = "\u26A1", unit = "\u043A\u0412\u0442\u00B7\u0447", decimalDigits = 0, color = "orange", enabled = true, model = "", apartmentId = DEFAULT_APARTMENT_ID),
            MeterConfig(id = "gas", name = "\u0413\u0430\u0437", icon = "\uD83D\uDCA8", unit = "\u043C\u00B3", decimalDigits = 3, color = "green", enabled = true, model = "", apartmentId = DEFAULT_APARTMENT_ID),
            MeterConfig(id = "heat", name = "\u041E\u0442\u043E\u043F\u043B\u0435\u043D\u0438\u0435", icon = "\uD83C\uDF21\uFE0F", unit = "\u0413\u043A\u0430\u043B", decimalDigits = 3, color = "pink", enabled = true, model = "", apartmentId = DEFAULT_APARTMENT_ID)
        )
    }

    private fun defaultConfigsForApartment(apartmentId: String): List<MeterConfig> {
        return if (apartmentId == DEFAULT_APARTMENT_ID) {
            defaultMeterConfigsDefinition()
        } else {
            buildApartmentDefaultMetersDefinition(apartmentId)
        }
    }

    private fun buildApartmentDefaultMeters(apartmentId: String): List<MeterConfig> {
        return listOf(
            MeterConfig(id = "${apartmentId}_water_cold", name = "\u0425\u043E\u043B\u043E\u0434\u043D\u0430\u044F \u0432\u043E\u0434\u0430", icon = "\uD83D\uDCA7", unit = "\u043C\u00B3", decimalDigits = 3, color = "blue", enabled = true, model = "", apartmentId = apartmentId),
            MeterConfig(id = "${apartmentId}_water_hot", name = "\u0413\u043E\u0440\u044F\u0447\u0430\u044F \u0432\u043E\u0434\u0430", icon = "\uD83D\uDD25", unit = "\u043C\u00B3", decimalDigits = 3, color = "red", enabled = true, model = "", apartmentId = apartmentId),
            MeterConfig(id = "${apartmentId}_electricity", name = "\u042D\u043B\u0435\u043A\u0442\u0440\u0438\u0447\u0435\u0441\u0442\u0432\u043E", icon = "\u26A1", unit = "\u043A\u0412\u0442\u00B7\u0447", decimalDigits = 0, color = "orange", enabled = true, model = "", apartmentId = apartmentId),
            MeterConfig(id = "${apartmentId}_gas", name = "\u0413\u0430\u0437", icon = "\uD83D\uDCA8", unit = "\u043C\u00B3", decimalDigits = 3, color = "green", enabled = true, model = "", apartmentId = apartmentId),
            MeterConfig(id = "${apartmentId}_heat", name = "\u041E\u0442\u043E\u043F\u043B\u0435\u043D\u0438\u0435", icon = "\uD83C\uDF21\uFE0F", unit = "\u0413\u043A\u0430\u043B", decimalDigits = 3, color = "pink", enabled = true, model = "", apartmentId = apartmentId)
        )
    }

    private fun JSONObject.toApartment(): Apartment {
        return Apartment(
            id = optString("id"),
            name = optString("name")
        )
    }

    private fun JSONObject.toMeterConfig(): MeterConfig {
        return MeterConfig(
            id = optString("id"),
            name = optString("name"),
            icon = optString("icon"),
            unit = optString("unit"),
            decimalDigits = optInt("decimalDigits", 0).coerceIn(0, 3),
            color = optString("color"),
            enabled = optBoolean("enabled", true),
            order = optInt("order", 0),
            model = optString("model"),
            apartmentId = optString("apartmentId", DEFAULT_APARTMENT_ID),
            tariffType = optString("tariffType", MeterTariffType.SINGLE.name).let {
                MeterTariffType.entries.firstOrNull { type -> type.name == it } ?: MeterTariffType.SINGLE
            },
            verificationDate = optLong("verificationDate", 0L),
            validityYears = optInt("validityYears", 4)
        )
    }
}
