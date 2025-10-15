package co.nisari.katisnar.presentation.util

import android.text.InputFilter
import android.text.Spanned

/**
 * InputFilter that restricts user input to a given [min]-[max] range for decimal numbers.
 * Allows intermediate states like "-" or "." while typing so the user can finish the input.
 */
class DoubleRangeInputFilter(
    private val min: Double,
    private val max: Double
) : InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val destLen = dest?.length ?: 0
        val safeStart = dstart.coerceIn(0, destLen)
        val safeEnd = dend.coerceIn(0, destLen)

        val prefix = dest?.subSequence(0, safeStart)?.toString() ?: ""
        val middle = source?.subSequence(start, end)?.toString() ?: ""
        val suffix = dest?.subSequence(safeEnd, destLen)?.toString() ?: ""

        val newValue = prefix + middle + suffix

        if (newValue.isBlank() || newValue == "-" || newValue == "." || newValue == "-.") {
            return null
        }

        val number = newValue.toDoubleOrNull() ?: return ""
        return if (number in min..max) null else ""
    }
}
