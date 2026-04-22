package ru.pepega.meterapp3.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.R
import ru.pepega.meterapp3.ui.settings.components.SettingsSectionHeader

@Composable
fun BackupSettingsSection(
    modifier: Modifier = Modifier,
    sectionContainerColor: Color,
    sectionContentColor: Color,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onExportClick: () -> Unit,
    onExportCsvClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = sectionContainerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SettingsSectionHeader(
                title = stringResource(R.string.backup_settings_title),
                description = stringResource(R.string.backup_settings_description),
                expanded = expanded,
                sectionContentColor = sectionContentColor,
                onToggleExpanded = onToggleExpanded
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onExportCsvClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.export_csv_action))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onExportClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(stringResource(R.string.export_backup_action))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onImportClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(stringResource(R.string.import_backup_action))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.backup_settings_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = sectionContentColor.copy(alpha = 0.75f)
                )
            }
        }
    }
}
