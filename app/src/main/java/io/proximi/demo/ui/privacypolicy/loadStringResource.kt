package io.proximi.demo.ui.privacypolicy

import android.content.Context
import android.util.TypedValue
import android.webkit.WebView
import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import io.proximi.demo.R

fun WebView.loadStringResource(@StringRes stringId: Int) {
    val backgroundColor = context.getColorFromAttr(android.R.attr.colorBackground).getColorRgbString()
    val textColor = context.getColorFromAttr(R.attr.colorPrivacyPolicy).getColorRgbString()
    val titleTextColor = context.getColorFromAttr(R.attr.colorOnPrimary).getColorRgbString()
    val html = String.format(
        "<body style=\"background-color: %s; color: %s; font-size: 0.9em; margin: 0 0 0 0; padding: 0 0 0 0;\">" +
                "<style>" +
                    "ul { padding-left: 1.5em }" +
                    "h2 { color: %s; font-size: 1em; margin: 0 0 0.4em 0; font-weight: bold; }" +
                "</style>" +
                "%s" +
        "</body>",
        backgroundColor,
        textColor,
        titleTextColor,
        context.getString(stringId)
    )
    loadData(html, "text/html", "UTF-8")
}

private fun Context.getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

private fun Int.getColorRgbString(): String {
    val r = (this shr 16 and 0xff)
    val g = (this shr 8 and 0xff)
    val b = (this and 0xff)
    val a = (this shr 24 and 0xff)
    return "rgb($r, $g, $b)"
}