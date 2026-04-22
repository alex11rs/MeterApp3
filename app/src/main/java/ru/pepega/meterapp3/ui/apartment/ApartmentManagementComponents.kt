package ru.pepega.meterapp3.ui.apartment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.pepega.meterapp3.Apartment
import ru.pepega.meterapp3.R

@Composable
fun ApartmentManagementHeader(
    canAddApartment: Boolean,
    onBack: () -> Unit,
    onAddApartment: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    stringResource(R.string.back),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = stringResource(R.string.apartments_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        if (canAddApartment) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        },
                        CircleShape
                    )
                    .clickable(enabled = canAddApartment) { onAddApartment() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_apartment),
                    modifier = Modifier.size(18.dp),
                    tint = if (canAddApartment) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    }
                )
            }
        }
    }
}

@Composable
fun ApartmentListCard(
    apartment: Apartment,
    isActive: Boolean,
    onActivate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember(apartment.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onActivate() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = apartment.name,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isActive) {
                        stringResource(R.string.apartment_active)
                    } else {
                        stringResource(R.string.apartment_tap_to_activate)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.apartment_rename)) },
                        onClick = {
                            showMenu = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.apartment_delete)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ApartmentDeleteDialog(
    apartment: Apartment,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.apartment_delete)) },
        text = { Text(stringResource(R.string.apartment_delete_message, apartment.name)) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ApartmentNameDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it.take(40) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                label = { Text(stringResource(R.string.apartment_name_label)) }
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(value.trim()) }, enabled = value.isNotBlank()) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
