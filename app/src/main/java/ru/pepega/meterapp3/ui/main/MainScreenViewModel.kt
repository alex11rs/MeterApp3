package ru.pepega.meterapp3.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ru.pepega.meterapp3.Apartment
import ru.pepega.meterapp3.AppPreferences
import ru.pepega.meterapp3.MeterConfig
import ru.pepega.meterapp3.MeterData
import ru.pepega.meterapp3.MeterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
data class MainScreenStreamState(
    val apartments: List<Apartment> = emptyList(),
    val activeApartmentId: String = "",
    val configs: List<MeterConfig> = emptyList(),
    val meterDataById: Map<String, MeterData> = emptyMap()
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainScreenViewModel(
    private val repository: MeterRepository
) : ViewModel() {
    private val activeApartmentId = MutableStateFlow("")

    init {
        viewModelScope.launch {
            activeApartmentId.value = repository.getActiveApartmentId()
        }
    }

    val uiState: StateFlow<MainScreenStreamState> = combine(
        repository.observeApartments(),
        activeApartmentId,
        activeApartmentId.flatMapLatest(repository::observeMeterConfigs),
        repository.observeAllMeterData()
    ) { apartments, currentApartmentId, configs, meterDataById ->
        val resolvedApartmentId = when {
            apartments.isEmpty() -> currentApartmentId
            apartments.any { it.id == currentApartmentId } -> currentApartmentId
            else -> apartments.first().id
        }
        if (resolvedApartmentId != currentApartmentId) {
            activeApartmentId.value = resolvedApartmentId
            repository.setActiveApartmentId(resolvedApartmentId)
        }
        MainScreenStreamState(
            apartments = apartments,
            activeApartmentId = resolvedApartmentId,
            configs = configs,
            meterDataById = meterDataById
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainScreenStreamState()
    )

    fun selectApartment(apartmentId: String) {
        repository.setActiveApartmentId(apartmentId)
        activeApartmentId.value = apartmentId
    }

    fun syncActiveApartment() {
        viewModelScope.launch {
            activeApartmentId.value = repository.getActiveApartmentId()
        }
    }
}

class MainScreenViewModelFactory(
    private val repository: MeterRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainScreenViewModel(repository) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
