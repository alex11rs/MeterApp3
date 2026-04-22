package ru.pepega.meterapp3.ui.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import ru.pepega.meterapp3.theme.ThemePreset
import ru.pepega.meterapp3.theme.labelResId
import ru.pepega.meterapp3.ui.settings.components.SettingsSectionHeader

@Composable
fun ThemeSettingsSection(
    modifier: Modifier = Modifier,
    sectionContainerColor: Color,
    sectionContentColor: Color,
    themeOptions: List<ThemePreset>,
    selectedTheme: ThemePreset,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onThemeSelected: (ThemePreset) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = sectionContainerColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SettingsSectionHeader(
                title = stringResource(R.string.theme_title),
                description = stringResource(R.string.theme_current, stringResource(selectedTheme.labelResId())),
                expanded = expanded,
                sectionContentColor = sectionContentColor,
                onToggleExpanded = onToggleExpanded
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                themeOptions.forEachIndexed { index, themePreset ->
                    val isSelected = selectedTheme == themePreset
                    val themePreviewColors = when (themePreset) {
                        ThemePreset.LIGHT -> listOf(Color(0xFF1565C0), Color(0xFFD6E8FF), Color(0xFF00695C))
                        ThemePreset.ROSE -> listOf(Color(0xFFB24A6B), Color(0xFFFFD9E3), Color(0xFF9A6040))
                        ThemePreset.MINT -> listOf(Color(0xFF2B7A68), Color(0xFFB8F2E1), Color(0xFF4D6477))
                        ThemePreset.DARK -> listOf(Color(0xFF9FCAFF), Color(0xFF00497F), Color(0xFF8DD5C6))
                        ThemePreset.SAND -> listOf(Color(0xFF9C5A1A), Color(0xFFFFDCC0), Color(0xFF69602F))
                        ThemePreset.FOREST -> listOf(Color(0xFF86D35B), Color(0xFF16361E), Color(0xFFFF8A3C))
                        ThemePreset.TOXIC -> listOf(Color(0xFFD3FF34), Color(0xFF00F0B5), Color(0xFFFF4FD8))
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(themePreset) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
                        },
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.width(42.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                themePreviewColors.forEach { previewColor ->
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(previewColor, CircleShape)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(themePreset.labelResId()),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = stringResource(
                                        when (themePreset) {
                                            ThemePreset.LIGHT -> R.string.theme_light_description
                                            ThemePreset.ROSE -> R.string.theme_rose_description
                                            ThemePreset.MINT -> R.string.theme_mint_description
                                            ThemePreset.DARK -> R.string.theme_dark_description
                                            ThemePreset.SAND -> R.string.theme_sand_description
                                            ThemePreset.FOREST -> R.string.theme_forest_description
                                            ThemePreset.TOXIC -> R.string.theme_toxic_description
                                        }
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = sectionContentColor.copy(alpha = 0.72f),
                                    maxLines = 1
                                )
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { onThemeSelected(themePreset) }
                            )
                        }
                    }

                    if (index < themeOptions.lastIndex) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}
