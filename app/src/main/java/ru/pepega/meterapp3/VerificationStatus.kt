package ru.pepega.meterapp3

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

sealed class VerificationStatus {
    object NOT_SET : VerificationStatus()
    object OK : VerificationStatus()
    data class EXPIRING(val daysLeft: Int) : VerificationStatus()
    data class EXPIRING_SOON(val daysLeft: Int) : VerificationStatus()
    data class EXPIRED(val daysLeft: Int) : VerificationStatus()

    fun getColor(): Color = when (this) {
        NOT_SET -> Color.Gray
        OK -> Color.Green
        is EXPIRING -> Color.Yellow
        is EXPIRING_SOON -> Color(0xFFFF9800)
        is EXPIRED -> Color.Red
    }

    fun getIcon(): String = when (this) {
        NOT_SET -> "\u26AA"
        OK -> "\u2705"
        is EXPIRING -> "\u23F3"
        is EXPIRING_SOON -> "\u26A0\uFE0F"
        is EXPIRED -> "\u274C"
    }

    fun getMessage(meterName: String): String = when (this) {
        NOT_SET -> "\u041F\u043E\u0432\u0435\u0440\u043A\u0430 \u043D\u0435 \u0443\u043A\u0430\u0437\u0430\u043D\u0430"
        OK -> "\u041F\u043E\u0432\u0435\u0440\u043A\u0430 \u0432 \u043F\u043E\u0440\u044F\u0434\u043A\u0435"
        is EXPIRING -> "\u041F\u043E\u0432\u0435\u0440\u043A\u0430 \u0441\u0447\u0435\u0442\u0447\u0438\u043A\u0430 \"$meterName\" \u0438\u0441\u0442\u0435\u043A\u0430\u0435\u0442 \u0447\u0435\u0440\u0435\u0437 $daysLeft \u0434\u043D\u0435\u0439"
        is EXPIRING_SOON -> "\u041F\u043E\u0432\u0435\u0440\u043A\u0430 \u0441\u0447\u0435\u0442\u0447\u0438\u043A\u0430 \"$meterName\" \u0438\u0441\u0442\u0435\u043A\u0430\u0435\u0442 \u0447\u0435\u0440\u0435\u0437 $daysLeft \u0434\u043D\u0435\u0439"
        is EXPIRED -> "\u041F\u043E\u0432\u0435\u0440\u043A\u0430 \u0441\u0447\u0435\u0442\u0447\u0438\u043A\u0430 \"$meterName\" \u043F\u0440\u043E\u0441\u0440\u043E\u0447\u0435\u043D\u0430 \u043D\u0430 ${abs(daysLeft)} \u0434\u043D\u0435\u0439"
    }
}

data class VerificationInfo(
    val status: VerificationStatus,
    val expiryDate: Long,
    val daysLeft: Int,
    val progress: Float
)

fun getVerificationStatus(verificationDate: Long, validityYears: Int): VerificationStatus {
    return getVerificationInfo(verificationDate, validityYears).status
}

fun getVerificationInfo(verificationDate: Long, validityYears: Int): VerificationInfo {
    if (verificationDate == 0L) {
        return VerificationInfo(
            status = VerificationStatus.NOT_SET,
            expiryDate = 0L,
            daysLeft = 0,
            progress = 0f
        )
    }

    val now = System.currentTimeMillis()
    val calendar = java.util.Calendar.getInstance().apply {
        timeInMillis = verificationDate
        add(java.util.Calendar.YEAR, validityYears)
    }
    val expiryDate = calendar.timeInMillis
    val daysLeft = ((expiryDate - now) / (1000 * 60 * 60 * 24)).toInt()
    val progress = (if (expiryDate > verificationDate) {
        val total = expiryDate - verificationDate
        val elapsed = now - verificationDate
        (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    })

    val status = when {
        daysLeft < 0 -> VerificationStatus.EXPIRED(daysLeft)
        daysLeft < 30 -> VerificationStatus.EXPIRING_SOON(daysLeft)
        daysLeft < 90 -> VerificationStatus.EXPIRING(daysLeft)
        else -> VerificationStatus.OK
    }

    return VerificationInfo(
        status = status,
        expiryDate = expiryDate,
        daysLeft = daysLeft,
        progress = progress
    )
}

fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return "\u041D\u0435 \u0443\u043A\u0430\u0437\u0430\u043D\u0430"
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return format.format(date)
}

fun formatYears(value: Int): String {
    val lastTwoDigits = value % 100
    val lastDigit = value % 10
    val suffix = when {
        lastTwoDigits in 11..14 -> "лет"
        lastDigit == 1 -> "год"
        lastDigit in 2..4 -> "года"
        else -> "лет"
    }
    return "$value $suffix"
}
