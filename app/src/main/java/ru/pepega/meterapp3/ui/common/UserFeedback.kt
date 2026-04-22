package ru.pepega.meterapp3.ui.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

fun showShortUserMessage(
    context: Context,
    message: String
) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun copyTextToClipboard(
    context: Context,
    label: String,
    text: String
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}
