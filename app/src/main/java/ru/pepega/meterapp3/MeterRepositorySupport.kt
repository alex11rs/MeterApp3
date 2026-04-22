package ru.pepega.meterapp3

import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset

private val CP1251: Charset = Charset.forName("windows-1251")

private data class DefaultMeterTemplate(
    val idSuffix: String,
    val name: String,
    val icon: String,
    val unit: String,
    val color: String,
    val decimalDigits: Int
)

private val DEFAULT_METER_TEMPLATES = listOf(
    DefaultMeterTemplate("water_cold", "Холодная вода", "💧", "м³", "blue", 3),
    DefaultMeterTemplate("water_hot", "Горячая вода", "🔥", "м³", "red", 3),
    DefaultMeterTemplate("electricity", "Электричество", "⚡", "кВт·ч", "orange", 0),
    DefaultMeterTemplate("gas", "Газ", "💨", "м³", "green", 3),
    DefaultMeterTemplate("heat", "Отопление", "🌡️", "Гкал", "pink", 3)
)

internal fun suggestedDecimalDigitsForUnit(unit: String): Int {
    val normalizedUnit = repairMojibake(unit).trim().lowercase()
    return when {
        normalizedUnit.contains("м³") || normalizedUnit.contains("m³") -> 3
        normalizedUnit.contains("гкал") -> 3
        normalizedUnit.contains("квт") -> 0
        else -> 0
    }
}

internal fun defaultApartmentDefinition(): Apartment = Apartment(
    DEFAULT_APARTMENT_ID,
    "Основная"
)

internal fun defaultMeterConfigsDefinition(): List<MeterConfig> {
    return DEFAULT_METER_TEMPLATES.map { template ->
        MeterConfig(
            id = template.idSuffix,
            name = template.name,
            icon = template.icon,
            unit = template.unit,
            decimalDigits = template.decimalDigits,
            color = template.color,
            enabled = true,
            model = "",
            apartmentId = DEFAULT_APARTMENT_ID
        )
    }
}

internal fun buildApartmentDefaultMetersDefinition(apartmentId: String): List<MeterConfig> {
    return DEFAULT_METER_TEMPLATES.map { template ->
        MeterConfig(
            id = "${apartmentId}_${template.idSuffix}",
            name = template.name,
            icon = template.icon,
            unit = template.unit,
            decimalDigits = template.decimalDigits,
            color = template.color,
            enabled = true,
            model = "",
            apartmentId = apartmentId
        )
    }
}

internal fun normalizeApartmentModel(apartment: Apartment): Apartment {
    val repairedName = repairMojibake(apartment.name)
    return if (apartment.id == DEFAULT_APARTMENT_ID && looksMojibake(apartment.name)) {
        apartment.copy(name = "Основная")
    } else if (repairedName != apartment.name) {
        apartment.copy(name = repairedName)
    } else {
        apartment
    }
}

internal fun normalizeMeterConfigModel(config: MeterConfig): MeterConfig {
    val template = defaultTemplateForId(config.id)
    if (template != null && (looksMojibake(config.name) || looksMojibake(config.icon) || looksMojibake(config.unit))) {
        return config.copy(
            name = template.name,
            icon = template.icon,
            unit = template.unit,
            color = if (config.color.isBlank()) template.color else config.color
        )
    }

    val repairedName = repairMojibake(config.name)
    val repairedIcon = repairMojibake(config.icon)
    val repairedUnit = repairMojibake(config.unit)

    return if (
        repairedName != config.name ||
        repairedIcon != config.icon ||
        repairedUnit != config.unit
    ) {
        config.copy(
            name = repairedName,
            icon = repairedIcon,
            unit = repairedUnit
        )
    } else {
        config
    }
}

private fun defaultTemplateForId(id: String): DefaultMeterTemplate? {
    return DEFAULT_METER_TEMPLATES.firstOrNull { template ->
        id == template.idSuffix || id.endsWith("_${template.idSuffix}")
    }
}

private fun looksMojibake(text: String): Boolean {
    if (text.isBlank()) return false
    return text.contains("Р") || text.contains("С") || text.contains("вЂ") || text.contains("рџ")
}

private fun repairMojibake(text: String): String {
    if (!looksMojibake(text)) return text
    return runCatching {
        String(text.toByteArray(CP1251), Charsets.UTF_8)
    }.getOrDefault(text)
}

internal fun JSONObject.toApartmentModel(): Apartment {
    return normalizeApartmentModel(
        Apartment(
            id = optString("id"),
            name = optString("name")
        )
    )
}

internal fun JSONObject.toLegacyMeterConfig(): MeterConfig {
    return normalizeMeterConfigModel(
        MeterConfig(
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
    )
}

internal fun parseLegacyMeterHistory(raw: String): List<MeterReadingHistoryEntry> {
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
