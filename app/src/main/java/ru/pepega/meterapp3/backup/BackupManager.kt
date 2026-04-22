package ru.pepega.meterapp3.backup

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import ru.pepega.meterapp3.Apartment
import ru.pepega.meterapp3.DEFAULT_APARTMENT_ID
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.MeterData
import ru.pepega.meterapp3.MeterReadingHistoryEntry
import ru.pepega.meterapp3.MeterTariffType
import ru.pepega.meterapp3.meterRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {
    suspend fun exportBackup(context: Context, uri: Uri, prefs: SharedPreferences) {
        val repository = meterRepository(context, prefs)
        val configs = repository.getAllMeterConfigs()

        val root = JSONObject().apply {
            put("version", 2)
            put("exportedAt", System.currentTimeMillis())
            put("activeApartmentId", repository.getActiveApartmentId())
            put(
                "apartments",
                JSONArray().apply {
                    repository.getApartments().forEach { apartment ->
                        put(
                            JSONObject().apply {
                                put("id", apartment.id)
                                put("name", apartment.name)
                            }
                        )
                    }
                }
            )
            put(
                "meterConfigs",
                JSONArray().apply {
                    configs.forEach { config ->
                        put(
                            JSONObject().apply {
                                put("id", config.id)
                                put("name", config.name)
                                put("icon", config.icon)
                                put("unit", config.unit)
                                put("decimalDigits", config.decimalDigits)
                                put("color", config.color)
                                put("enabled", config.enabled)
                                put("order", config.order)
                                put("model", config.model)
                                put("apartmentId", config.apartmentId)
                                put("tariffType", config.tariffType.name)
                                put("verificationDate", config.verificationDate)
                                put("validityYears", config.validityYears)
                            }
                        )
                    }
                }
            )
            put(
                "meterData",
                JSONObject().apply {
                    configs.forEach { config ->
                        val data = repository.getMeterData(config.id)
                        put(
                            config.id,
                            JSONObject().apply {
                                put("current", data.current)
                                put("previous", data.previous)
                                put("tariff", data.tariff)
                                put("currentNight", data.currentNight)
                                put("previousNight", data.previousNight)
                                put("nightTariff", data.nightTariff)
                                put("lastUpdate", data.lastUpdate)
                                put(
                                    "history",
                                    JSONArray().apply {
                                        data.history.forEach { entry ->
                                            put(
                                                JSONObject().apply {
                                                    put("value", entry.value)
                                                    put("timestamp", entry.timestamp)
                                                    entry.secondaryValue?.let { put("secondaryValue", it) }
                                                }
                                            )
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
            )
        }

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            OutputStreamWriter(stream, StandardCharsets.UTF_8).use { writer ->
                writer.write(root.toString(2))
            }
        } ?: error("Cannot open output stream")
    }

    suspend fun exportCsv(context: Context, uri: Uri, prefs: SharedPreferences) {
        val repository = meterRepository(context, prefs)
        val configs = repository.getAllMeterConfigs().filter { it.enabled }
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val sep = ";"

        data class Reading(val value: Float, val secondaryValue: Float?, val consumption: Float, val nightConsumption: Float)
        val table = mutableMapOf<String, MutableMap<String, Reading>>()
        val allDates = mutableSetOf<String>()

        configs.forEach { config ->
            val data = repository.getMeterData(config.id)
            val isDual = config.tariffType == MeterTariffType.DUAL
            val allReadings = buildList {
                addAll(data.history)
                if (data.lastUpdate > 0) {
                    add(MeterReadingHistoryEntry(
                        value = data.current,
                        timestamp = data.lastUpdate,
                        secondaryValue = if (isDual) data.currentNight else null
                    ))
                }
            }.sortedBy { it.timestamp }

            allReadings.forEachIndexed { index, entry ->
                val prev = allReadings.getOrNull(index - 1)
                val date = dateFormat.format(Date(entry.timestamp))
                allDates.add(date)
                table.getOrPut(date) { mutableMapOf() }[config.id] = Reading(
                    value = entry.value,
                    secondaryValue = entry.secondaryValue,
                    consumption = if (prev != null) maxOf(0f, entry.value - prev.value) else 0f,
                    nightConsumption = if (isDual && prev != null) maxOf(0f, (entry.secondaryValue ?: 0f) - (prev.secondaryValue ?: 0f)) else 0f
                )
            }
        }

        val sb = StringBuilder()
        val headerCols = mutableListOf("Дата")
        configs.forEach { config ->
            if (config.tariffType == MeterTariffType.DUAL) {
                headerCols.add("${config.name} день (${config.unit})")
                headerCols.add("${config.name} ночь (${config.unit})")
                headerCols.add("${config.name} расход день")
                headerCols.add("${config.name} расход ночь")
            } else {
                headerCols.add("${config.name} (${config.unit})")
                headerCols.add("${config.name} расход")
            }
        }
        sb.appendLine(headerCols.joinToString(sep))

        val sortedDates = allDates.sortedWith(compareBy {
            runCatching { dateFormat.parse(it)?.time }.getOrNull() ?: 0L
        })
        sortedDates.forEach { date ->
            val row = mutableListOf(date)
            configs.forEach { config ->
                val r = table[date]?.get(config.id)
                if (config.tariffType == MeterTariffType.DUAL) {
                    row.add(if (r != null) String.format(Locale.getDefault(), "%.2f", r.value) else "")
                    row.add(if (r != null) String.format(Locale.getDefault(), "%.2f", r.secondaryValue ?: 0f) else "")
                    row.add(if (r != null && r.consumption > 0f) String.format(Locale.getDefault(), "%.2f", r.consumption) else "")
                    row.add(if (r != null && r.nightConsumption > 0f) String.format(Locale.getDefault(), "%.2f", r.nightConsumption) else "")
                } else {
                    row.add(if (r != null) String.format(Locale.getDefault(), "%.2f", r.value) else "")
                    row.add(if (r != null && r.consumption > 0f) String.format(Locale.getDefault(), "%.2f", r.consumption) else "")
                }
            }
            sb.appendLine(row.joinToString(sep))
        }

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            OutputStreamWriter(stream, StandardCharsets.UTF_8).use { writer ->
                writer.write("\uFEFF")
                writer.write(sb.toString())
            }
        } ?: error("Cannot open output stream")
    }

    suspend fun importBackup(context: Context, uri: Uri, prefs: SharedPreferences) {
        val jsonText = context.contentResolver.openInputStream(uri)?.use { stream ->
            BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).readText()
        } ?: error("Cannot open input stream")

        val root = JSONObject(jsonText)
        if (!root.has("meterConfigs") || !root.has("meterData")) {
            error("Invalid backup format")
        }

        val repository = meterRepository(context, prefs)
        val oldConfigs = repository.getAllMeterConfigs()
        oldConfigs.forEach { repository.resetMeterData(it.id) }

        prefs.edit().apply {
            remove("meter_configs")
            remove("apartments")
            remove("active_apartment_id")
        }.apply()

        val apartments = root.optJSONArray("apartments")?.let { array ->
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                Apartment(
                    id = item.optString("id"),
                    name = item.optString("name")
                )
            }
        }.orEmpty()
        repository.saveApartments(apartments.ifEmpty { listOf(Apartment(DEFAULT_APARTMENT_ID, "\u041E\u0441\u043D\u043E\u0432\u043D\u0430\u044F")) })

        val configsArray = root.getJSONArray("meterConfigs")
        val configs = List(configsArray.length()) { index ->
            val item = configsArray.getJSONObject(index)
            MeterConfig(
                id = item.optString("id"),
                name = item.optString("name"),
                icon = item.optString("icon"),
                unit = item.optString("unit"),
                decimalDigits = item.optInt("decimalDigits", 0).coerceIn(0, 3),
                color = item.optString("color", "blue"),
                enabled = item.optBoolean("enabled", true),
                order = item.optInt("order", 0),
                model = item.optString("model"),
                apartmentId = item.optString("apartmentId", DEFAULT_APARTMENT_ID),
                tariffType = item.optString("tariffType", MeterTariffType.SINGLE.name).let { raw ->
                    MeterTariffType.values().firstOrNull { it.name == raw } ?: MeterTariffType.SINGLE
                },
                verificationDate = item.optLong("verificationDate", 0L),
                validityYears = item.optInt("validityYears", 4)
            )
        }
        repository.saveAllMeterConfigs(configs)

        val dataObject = root.getJSONObject("meterData")
        configs.forEach { config ->
            val item = dataObject.optJSONObject(config.id) ?: return@forEach
            val historyArray = item.optJSONArray("history") ?: JSONArray()
            val history = List(historyArray.length()) { index ->
                val historyItem = historyArray.getJSONObject(index)
                MeterReadingHistoryEntry(
                    value = historyItem.optDouble("value").toFloat(),
                    timestamp = historyItem.optLong("timestamp"),
                    secondaryValue = if (historyItem.has("secondaryValue")) {
                        historyItem.optDouble("secondaryValue").toFloat()
                    } else {
                        null
                    }
                )
            }

            repository.saveMeterData(
                config.id,
                MeterData(
                    current = item.optDouble("current").toFloat(),
                    previous = item.optDouble("previous").toFloat(),
                    tariff = item.optDouble("tariff", 5.0).toFloat(),
                    currentNight = item.optDouble("currentNight").toFloat(),
                    previousNight = item.optDouble("previousNight").toFloat(),
                    nightTariff = item.optDouble("nightTariff").toFloat(),
                    lastUpdate = item.optLong("lastUpdate")
                )
            )
            repository.replaceHistory(config.id, history)
        }

        root.optString("activeApartmentId").takeIf { it.isNotBlank() }?.let {
            repository.setActiveApartmentId(it)
        }
    }
}
