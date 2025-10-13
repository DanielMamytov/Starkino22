package co.nisari.katisnar.presentation.ui.starlocation

import android.widget.TextView

fun TextView.setTextIfDifferent(newText: CharSequence?) {
    val current = text?.toString() ?: ""
    val next = newText?.toString() ?: ""
    if (current != next) text = next
}
