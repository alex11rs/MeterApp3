package ru.pepega.meterapp3.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.R
import ru.pepega.meterapp3.ui.settings.components.SettingsSectionHeader

@Composable
fun ScannerSettingsSection(
    modifier: Modifier = Modifier,
    sectionContainerColor: Color,
    sectionContentColor: Color,
    scannerEnabled: Boolean,
    scannerApiKey: String,
    hasScannerApiKey: Boolean,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onScannerEnabledChange: (Boolean) -> Unit,
    onScannerApiKeyChange: (String) -> Unit,
    onImportScannerKeyClick: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = sectionContainerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SettingsSectionHeader(
                title = stringResource(R.string.scanner_settings_title),
                description = stringResource(R.string.scanner_mode_google_vision_label),
                expanded = expanded,
                sectionContentColor = sectionContentColor,
                onToggleExpanded = onToggleExpanded
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.scanner_settings_enabled_label),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.scanner_settings_enabled_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = sectionContentColor.copy(alpha = 0.72f)
                        )
                    }
                    Switch(
                        checked = scannerEnabled,
                        onCheckedChange = onScannerEnabledChange,
                        enabled = hasScannerApiKey || scannerEnabled
                    )
                }


                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (hasScannerApiKey) {
                        stringResource(R.string.scanner_key_present)
                    } else {
                        stringResource(R.string.scanner_key_missing)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = sectionContentColor.copy(alpha = 0.72f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = scannerApiKey,
                    onValueChange = onScannerApiKeyChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(stringResource(R.string.scanner_api_key_label)) },
                    placeholder = { Text(stringResource(R.string.scanner_api_key_hint)) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onImportScannerKeyClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.scanner_import_key_action))
                }
            }
        }
    }
}

