package ru.pepega.meterapp3.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AppDialogHeader(
    title: String,
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 28.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingContent?.invoke()
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            if (trailingContent != null) {
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    trailingContent()
                }
            }
        }
    }
}
