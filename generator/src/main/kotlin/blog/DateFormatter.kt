package blog

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

object DateFormatter {
  private val displayFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH)

  fun formatForDisplay(dateString: String): String {
    if (dateString.isBlank()) return ""

    return try {
      val offsetDateTime = OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      offsetDateTime.toLocalDate().format(displayFormatter)
    } catch (e: DateTimeParseException) {
      try {
        LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE).format(displayFormatter)
      } catch (e: DateTimeParseException) {
        throw IllegalArgumentException("Invalid date format: $dateString")
      }
    }
  }

  fun toIsoDate(dateString: String): String = dateString.substringBefore("T")
}
