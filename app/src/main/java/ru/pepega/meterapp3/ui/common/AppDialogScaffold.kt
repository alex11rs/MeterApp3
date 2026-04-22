package ru.pepega.meterapp3.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.pepega.meterapp3.R

@Composable
fun AppDialogScaffold(
    title: String,
    onDismissRequest: () -> Unit,
    showScrollHint: Boolean,
    contentScrollState: androidx.compose.foundation.ScrollState,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val dialogMaxHeight = configuration.screenHeightDp.dp * 0.88f

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 24.dp)
                .heightIn(max = dialogMaxHeight)
                .widthIn(max = 480.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AppDialogHeader(
                        title = title
                    )
                    if (showScrollHint) {
                        Text(
                            text = stringResource(R.string.scroll_down_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HorizontalDivider(color = androidx.compose.ui.graphics.Color.Transparent)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(contentScrollState)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    content = content
                )

                HorizontalDivider(color = androidx.compose.ui.graphics.Color.Transparent)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    content = actions
                )
            }
        }
    }
}
