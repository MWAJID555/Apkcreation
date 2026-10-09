package com.example.util

import com.example.model.FormattedTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {

    fun format(elapsedMs: Long): FormattedTime {
        val totalHundredths = (elapsedMs / 10).coerceAtLeast(0)
        val cs = totalHundredths % 100
        val totalSeconds = elapsedMs / 1000
        val sec = totalSeconds % 60
        val totalMinutes = totalSeconds / 60
        val min = totalMinutes % 60
        val hours = totalMinutes / 60

        val hStr = String.format(Locale.US, "%02d", hours)
        val mStr = String.format(Locale.US, "%02d", min)
        val sStr = String.format(Locale.US, "%02d", sec)
        val csStr = String.format(Locale.US, "%02d", cs)

        val fullStr = if (hours > 0) {
            "$hStr:$mStr:$sStr.$csStr"
        } else {
            "$mStr:$sStr.$csStr"
        }

        return FormattedTime(
            hours = hStr,
            minutes = mStr,
            seconds = sStr,
            hundredths = csStr,
            fullFormatted = fullStr
        )
    }

    fun formatCompact(elapsedMs: Long): String {
        val f = format(elapsedMs)
        return f.fullFormatted
    }

    fun formatDelta(deltaMs: Long): String {
        if (deltaMs == 0L) return "±0.00"
        val sign = if (deltaMs > 0) "+" else "-"
        val absMs = kotlin.math.abs(deltaMs)
        val seconds = absMs / 1000.0
        return String.format(Locale.US, "%s%.2fs", sign, seconds)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
