package ru.pepega.meterapp3

const val DEFAULT_APARTMENT_ID = "apartment_main"

enum class MeterTariffType {
    SINGLE,
    DUAL
}

enum class ReadingField {
    DAY,
    NIGHT
}

data class Apartment(
    val id: String,
    val name: String
)

data class MeterConfig(
    val id: String,
    val name: String,
    val icon: String,
    val unit: String,
    val decimalDigits: Int = 0,
    val color: String = "blue",
    val enabled: Boolean = true,
    val order: Int = 0,
    val model: String = "",
    val apartmentId: String = DEFAULT_APARTMENT_ID,
    val tariffType: MeterTariffType = MeterTariffType.SINGLE,
    val verificationDate: Long = 0L,
    val validityYears: Int = 4
)

data class MeterData(
    val current: Float = 0f,
    val previous: Float = 0f,
    val tariff: Float = 5.0f,
    val currentNight: Float = 0f,
    val previousNight: Float = 0f,
    val nightTariff: Float = 0f,
    val lastUpdate: Long = 0L,
    val history: List<MeterReadingHistoryEntry> = emptyList()
)

data class MeterReadingHistoryEntry(
    val value: Float,
    val timestamp: Long,
    val secondaryValue: Float? = null
)

data class MeterSummary(
    val id: String,
    val icon: String,
    val name: String,
    val consumption: Float,
    val dayConsumption: Float = consumption,
    val nightConsumption: Float = 0f,
    val currentReading: Float = 0f,
    val currentNightReading: Float = 0f,
    val tariffType: MeterTariffType = MeterTariffType.SINGLE,
    val unit: String,
    val cost: Float,
    val lastUpdate: Long
)

data class TotalSummary(
    val totalCost: Float,
    val totalConsumption: Float,
    val meters: List<MeterSummary>
)

data class MonthHistory(
    val month: String,
    val meters: List<MeterHistoryItem>
)

data class MeterHistoryItem(
    val icon: String,
    val name: String,
    val reading: Float,
    val nightReading: Float = 0f,
    val unit: String,
    val consumption: Float,
    val dayConsumption: Float = consumption,
    val nightConsumption: Float = 0f,
    val tariffType: MeterTariffType = MeterTariffType.SINGLE,
    val cost: Float,
    val date: Long
)
