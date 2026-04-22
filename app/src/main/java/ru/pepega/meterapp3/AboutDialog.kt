package ru.pepega.meterapp3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.pepega.meterapp3.ui.common.AppDialogHeader

@Composable
fun AboutDialog(
    onShowGuide: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AboutDialogHeader(onShowGuide = onShowGuide) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.about_description),
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = stringResource(R.string.about_features_title),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = stringResource(R.string.about_features_list),
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = stringResource(R.string.about_version),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.understand))
            }
        }
    )
}

@Composable
private fun AboutDialogHeader(
    onShowGuide: () -> Unit
) {
    val guideContentDescription = stringResource(R.string.main_guide_open)

    AppDialogHeader(
        title = stringResource(R.string.about_title),
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
        },
        trailingContent = {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                tonalElevation = 1.dp,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onShowGuide)
                        .semantics {
                            contentDescription = guideContentDescription
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface
                    ) {
                        Text("?", fontSize = 18.sp)
                    }
                }
            }
        }
    )
}
