package ru.pepega.meterapp3

import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ru.pepega.meterapp3.ui.common.AppDialogScaffold

@Composable
fun MainGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val guideLines = remember {
        context.resources.getStringArray(R.array.main_guide_lines).toList()
    }
    val contentScrollState = rememberScrollState()
    val showScrollHint by remember {
        derivedStateOf {
            contentScrollState.maxValue > 0 && contentScrollState.value < contentScrollState.maxValue
        }
    }

    AppDialogScaffold(
        title = stringResource(R.string.main_guide_title),
        onDismissRequest = onDismiss,
        showScrollHint = showScrollHint,
        contentScrollState = contentScrollState,
        actions = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.understand))
            }
        }
    ) {
        guideLines.forEach { line ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
