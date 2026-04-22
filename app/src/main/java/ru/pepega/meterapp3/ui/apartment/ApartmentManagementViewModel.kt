package ru.pepega.meterapp3.ui.apartment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ru.pepega.meterapp3.Apartment
import ru.pepega.meterapp3.MeterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ApartmentManagementUiState(
    val apartments: List<Apartment> = emptyList(),
    val activeApartmentId: String = ""
) {
    val canAddApartment: Boolean
        get() = apartments.size < 8
}

class ApartmentManagementViewModel(
    private val repository: MeterRepository
) : ViewModel() {
    private val activeApartmentId = MutableStateFlow("")

    init {
        viewModelScope.launch {
            activeApartmentId.value = repository.getActiveApartmentId()
        }
    }

    val uiState: StateFlow<ApartmentManagementUiState> = combine(
        repository.observeApartments(),
        activeApartmentId
    ) { apartments, currentActiveApartmentId ->
        val resolvedApartmentId = apartments
            .firstOrNull { it.id == currentActiveApartmentId }
            ?.id
            ?: apartments.firstOrNull()?.id
            .orEmpty()
        if (resolvedApartmentId.isNotBlank() && resolvedApartmentId != currentActiveApartmentId) {
            repository.setActiveApartmentId(resolvedApartmentId)
            activeApartmentId.value = resolvedApartmentId
        }
        ApartmentManagementUiState(
            apartments = apartments,
            activeApartmentId = resolvedApartmentId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ApartmentManagementUiState()
    )

    fun activateApartment(apartmentId: String) {
        repository.setActiveApartmentId(apartmentId)
        activeApartmentId.value = apartmentId
    }

    fun addApartment(name: String) {
        viewModelScope.launch {
            val apartment = repository.addApartment(name)
            repository.setActiveApartmentId(apartment.id)
            activeApartmentId.value = apartment.id
        }
    }

    fun renameApartment(apartmentId: String, name: String) {
        viewModelScope.launch {
            repository.renameApartment(apartmentId, name)
        }
    }

    fun deleteApartment(apartmentId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteApartment(apartmentId)
            onResult(result)
        }
    }
}

class ApartmentManagementViewModelFactory(
    private val repository: MeterRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ApartmentManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ApartmentManagementViewModel(repository) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
