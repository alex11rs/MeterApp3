package ru.pepega.meterapp3

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

fun meterRepository(
    context: Context,
    prefs: SharedPreferences
): MeterRepository {
    return MeterRepository(context, prefs)
}

fun meterRepository(
    context: Context,
    appPreferences: AppPreferences
): MeterRepository {
    return meterRepository(context, appPreferences.sharedPreferences)
}

@Composable
fun rememberMeterRepository(): MeterRepository {
    val context = LocalContext.current
    val appPreferences = rememberAppPreferences()
    return remember(context, appPreferences) {
        meterRepository(context, appPreferences)
    }
}

@Composable
inline fun <reified VM : ViewModel> rememberViewModel(
    vararg keys: Any?,
    noinline factoryBuilder: () -> ViewModelProvider.Factory
): VM {
    val factory = remember(*keys) { factoryBuilder() }
    val viewModelKey = remember(*keys) {
        buildString {
            append(VM::class.qualifiedName ?: VM::class.java.name)
            keys.forEachIndexed { index, key ->
                append("|")
                append(index)
                append("=")
                append(key?.toString() ?: "null")
            }
        }
    }
    return viewModel(
        key = viewModelKey,
        factory = factory
    )
}
