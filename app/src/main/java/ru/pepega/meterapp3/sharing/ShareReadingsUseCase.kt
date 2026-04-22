package ru.pepega.meterapp3.sharing

import android.content.Context
import android.content.Intent
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.MeterRepository
import ru.pepega.meterapp3.MeterStatistics
import ru.pepega.meterapp3.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun shareReadings(
    context: Context,
    repository: MeterRepository,
    configs: List<MeterConfig>
) {
    val message = withContext(Dispatchers.IO) {
        val sb = StringBuilder("${context.getString(R.string.share_readings_title)}\n\n")
        configs.filter { it.enabled }.forEach { meter ->
            val data = repository.getMeterData(meter.id)
            val consumption = MeterStatistics.getTotalConsumption(meter, data)
            if (meter.tariffType == ru.pepega.meterapp3.MeterTariffType.DUAL) {
                sb.append("${meter.icon} ${meter.name}: \u0414 ${data.current} / \u041D ${data.currentNight} ${meter.unit}\n")
            } else {
                sb.append("${meter.icon} ${meter.name}: ${data.current} ${meter.unit}\n")
            }
            sb.append("   ${context.getString(R.string.consumption_label)}: ${String.format("%.2f", consumption)} ${meter.unit}\n")
        }
        sb.append("\n\u0414\u0430\u0442\u0430: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())}")
        sb.toString()
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_readings_subject))
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_send_via)))
}
