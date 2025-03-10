package net.rokoucha.superseisan.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

class ConvertersTest {
    private lateinit var converters: Converters

    private val jstZoneOffset = ZoneOffset.ofHours(9)

    @Before
    fun setup() {
        converters = Converters()
    }

    @Test
    fun fromTimestamp_WithNull() {
        val actual = converters.fromTimestamp(null)

        assertNull(actual)
    }

    @Test(expected = DateTimeParseException::class)
    fun fromTimestamp_WithEmptyString() {
        val actual = converters.fromTimestamp("")

        assertNull(actual)
    }

    @Test
    fun fromTimestamp_WithUTCString() {
        val actual = converters.fromTimestamp("2025-02-28T03:12:45Z")

        val expected = OffsetDateTime.of(2025, 2, 28, 3, 12, 45, 0, ZoneOffset.UTC)

        assertEquals(expected, actual)
    }

    @Test
    fun fromTimestamp_WithJSTString() {
        val actual = converters.fromTimestamp("2025-02-28T12:12:45+09:00")

        val expected = OffsetDateTime.of(2025, 2, 28, 12, 12, 45, 0, jstZoneOffset)


        assertEquals(expected, actual)
    }

    @Test
    fun toTimestamp_WithNull() {
        val actual = converters.toTimestamp(null)

        assertNull(actual)
    }

    @Test
    fun toTimestamp_UTCDateTime() {
        val actual =
            converters.toTimestamp(OffsetDateTime.of(2025, 2, 28, 3, 12, 45, 0, ZoneOffset.UTC))

        val expected = "2025-02-28T03:12:45Z"

        assertEquals(expected, actual)
    }

    @Test
    fun toTimestamp_JSTDateTime() {
        val actual =
            converters.toTimestamp(
                OffsetDateTime.of(2025, 2, 28, 12, 12, 45, 0, jstZoneOffset)
            )

        val expected = "2025-02-28T12:12:45+09:00"

        assertEquals(expected, actual)
    }
}
