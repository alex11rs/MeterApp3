package ru.pepega.meterapp3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.pepega.meterapp3.ui.apartment.ApartmentDeleteDialog
import ru.pepega.meterapp3.ui.apartment.ApartmentListCard
import ru.pepega.meterapp3.ui.apartment.ApartmentManagementHeader
import ru.pepega.meterapp3.ui.apartment.ApartmentManagementViewModel
import ru.pepega.meterapp3.ui.apartment.ApartmentManagementViewModelFactory
import ru.pepega.meterapp3.ui.apartment.ApartmentNameDialog
import ru.pepega.meterapp3.ui.common.showShortUserMessage

@Composable
fun ApartmentManagementScreen(
    onBack: () -> Unit,
    onChanged: () -> Unit
) {
    val context = LocalContext.current
    val repository = rememberMeterRepository()
    val viewModel: ApartmentManagementViewModel = rememberViewModel(repository) {
        ApartmentManagementViewModelFactory(repository)
    }
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var apartmentForRename by remember { mutableStateOf<Apartment?>(null) }
    var apartmentForDelete by remember { mutableStateOf<Apartment?>(null) }
    val apartments = uiState.apartments
    val activeApartmentId = uiState.activeApartmentId
    val canAddApartment = uiState.canAddApartment

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        ApartmentManagementHeader(
            canAddApartment = canAddApartment,
            onBack = onBack,
            onAddApartment = { showAddDialog = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        apartments.forEach { apartment ->
            val isActive = apartment.id == activeApartmentId

            ApartmentListCard(
                apartment = apartment,
                isActive = isActive,
                onActivate = {
                    viewModel.activateApartment(apartment.id)
                    onChanged()
                },
                onRename = { apartmentForRename = apartment },
                onDelete = { apartmentForDelete = apartment }
            )
        }
    }

    if (showAddDialog) {
        ApartmentNameDialog(
            title = stringResource(R.string.add_apartment),
            initialValue = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                if (name.isBlank()) return@ApartmentNameDialog
                viewModel.addApartment(name)
                showAddDialog = false
                onChanged()
                showShortUserMessage(context, context.getString(R.string.apartment_added))
            }
        )
    }

    apartmentForRename?.let { apartment ->
        ApartmentNameDialog(
            title = stringResource(R.string.apartment_rename),
            initialValue = apartment.name,
            onDismiss = { apartmentForRename = null },
            onConfirm = { name ->
                if (name.isBlank()) return@ApartmentNameDialog
                viewModel.renameApartment(apartment.id, name)
                apartmentForRename = null
                onChanged()
                showShortUserMessage(context, context.getString(R.string.apartment_renamed))
            }
        )
    }

    apartmentForDelete?.let { apartment ->
        ApartmentDeleteDialog(
            apartment = apartment,
            onDismiss = { apartmentForDelete = null },
            onConfirm = {
                viewModel.deleteApartment(apartment.id) { deleted ->
                    apartmentForDelete = null
                    if (deleted) {
                        onChanged()
                        showShortUserMessage(context, context.getString(R.string.apartment_deleted))
                    } else {
                        showShortUserMessage(context, context.getString(R.string.apartment_delete_last_error))
                    }
                }
            }
        )
    }
}
