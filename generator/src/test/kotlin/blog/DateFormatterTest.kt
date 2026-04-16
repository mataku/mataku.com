package blog

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DateFormatterTest {
    @Test
    fun `formats ISO local date for display`() {
        assertEquals("Monday, January 15, 2024", DateFormatter.formatForDisplay("2024-01-15"))
    }

    @Test
    fun `formats ISO offset datetime by keeping the offset-local date`() {
        assertEquals(
            "Monday, January 15, 2024",
            DateFormatter.formatForDisplay("2024-01-15T10:30:00+09:00"),
        )
    }

    @Test
    fun `formats UTC datetime with Z suffix for display`() {
        assertEquals(
            "Monday, January 15, 2024",
            DateFormatter.formatForDisplay("2024-01-15T00:00:00Z"),
        )
    }

    @Test
    fun `returns empty string for blank input`() {
        assertEquals("", DateFormatter.formatForDisplay(""))
        assertEquals("", DateFormatter.formatForDisplay("   "))
    }

    @Test
    fun `throws IllegalArgumentException for invalid date format`() {
        assertFailsWith<IllegalArgumentException> {
            DateFormatter.formatForDisplay("not a date")
        }
    }

    @Test
    fun `toIsoDate strips the time portion`() {
        assertEquals("2024-01-15", DateFormatter.toIsoDate("2024-01-15T10:30:00+09:00"))
    }

    @Test
    fun `toIsoDate returns the same string when there is no time portion`() {
        assertEquals("2024-01-15", DateFormatter.toIsoDate("2024-01-15"))
    }
}
