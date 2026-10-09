package com.example

import com.example.data.SessionRepository
import com.example.model.Lap
import com.example.util.TimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun timeFormatter_formatsMillisecondsAccurately() {
        val formattedZero = TimeFormatter.format(0L)
        assertEquals("00", formattedZero.minutes)
        assertEquals("00", formattedZero.seconds)
        assertEquals("00", formattedZero.hundredths)

        // 65 seconds and 450 ms = 1 min, 5 sec, 45 hundredths
        val formatted = TimeFormatter.format(65450L)
        assertEquals("01", formatted.minutes)
        assertEquals("05", formatted.seconds)
        assertEquals("45", formatted.hundredths)
        assertEquals("01:05.45", formatted.fullFormatted)
    }

    @Test
    fun timeFormatter_formatsHoursCorrectly() {
        // 3661 seconds and 200 ms = 1 hour, 1 min, 1 sec, 20 hundredths
        val formatted = TimeFormatter.format(3661200L)
        assertEquals("01", formatted.hours)
        assertEquals("01", formatted.minutes)
        assertEquals("01", formatted.seconds)
        assertEquals("20", formatted.hundredths)
        assertEquals("01:01:01.20", formatted.fullFormatted)
    }

    @Test
    fun sessionRepository_lapSerializationAndDeserialization() {
        val rawLaps = listOf(
            Lap(lapNumber = 1, durationMs = 15000L, splitTimeMs = 15000L),
            Lap(lapNumber = 2, durationMs = 12000L, splitTimeMs = 27000L),
            Lap(lapNumber = 3, durationMs = 18000L, splitTimeMs = 45000L)
        )

        val encoded = rawLaps.joinToString(",") { "${it.lapNumber}:${it.durationMs}:${it.splitTimeMs}" }
        val parsed = SessionRepository.parseLaps(encoded)

        assertEquals(3, parsed.size)
        assertEquals(1, parsed[0].lapNumber)
        assertEquals(15000L, parsed[0].durationMs)
        assertEquals(2, parsed[1].lapNumber)
        assertEquals(12000L, parsed[1].durationMs)
        assertTrue(parsed[1].isFastest) // 12000 is fastest
        assertTrue(parsed[2].isSlowest) // 18000 is slowest
    }
}
