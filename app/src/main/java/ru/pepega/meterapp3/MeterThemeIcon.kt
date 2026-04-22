package ru.pepega.meterapp3

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import ru.pepega.meterapp3.theme.ThemePreset

val SadMeterCardIconContainerSize: Dp = 64.dp
val SadMeterCardIconPadding: Dp = 0.dp
val SadMeterSummaryIconSize: Dp = 32.dp

@Composable
fun MeterThemeIcon(
    icon: String,
    meterConfig: MeterConfig?,
    fallbackTitle: String,
    fallbackUnit: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit
) {
    val themePreset = rememberAppPreferences().themePreset()
    val sadMeterIconRes = remember(themePreset, meterConfig, fallbackTitle, fallbackUnit) {
        if (themePreset == ThemePreset.FOREST) {
            resolveSadMeterIconRes(
                meterConfig = meterConfig,
                fallbackTitle = fallbackTitle,
                fallbackUnit = fallbackUnit
            )
        } else {
            null
        }
    }

    if (sadMeterIconRes != null) {
        Image(
            painter = painterResource(id = sadMeterIconRes),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = fontSize
            )
        }
    }
}

@DrawableRes
private fun resolveSadMeterIconRes(
    meterConfig: MeterConfig?,
    fallbackTitle: String,
    fallbackUnit: String
): Int? {
    val meterId = meterConfig?.id.orEmpty().lowercase()
    val meterName = (meterConfig?.name?.takeIf { it.isNotBlank() } ?: fallbackTitle).lowercase()
    val normalizedUnit = fallbackUnit.replace('⋅', '·')

    return when {
        meterId.contains("electric") || meterName.contains("элект") || normalizedUnit == "кВт·ч" ->
            R.drawable.sad_meter_electricity
        meterId.contains("heat") || meterName.contains("отоп") || fallbackUnit == "Гкал" ->
            R.drawable.sad_meter_heat
        meterId.contains("gas") || meterName.contains("газ") ->
            R.drawable.sad_meter_gas
        meterId.contains("water_hot") || (
            normalizedUnit == "м³" && (
                meterId.contains("hot") ||
                    meterName.contains("горяч") ||
                    meterName.contains("hot")
                )
            ) -> R.drawable.sad_meter_water_hot
        normalizedUnit == "м³" -> R.drawable.sad_meter_water_cold
        else -> null
    }
}
