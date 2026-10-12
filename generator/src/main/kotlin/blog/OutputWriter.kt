package blog

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

/** Writes generated content only when it differs from what is already on disk. */
object OutputWriter {
  fun writeIfChanged(
    path: Path,
    content: String,
  ): Boolean {
    val bytes = content.toByteArray()
    if (path.exists() && path.readBytes().contentEquals(bytes)) return false
    path.parent.createDirectories()
    path.writeBytes(bytes)
    return true
  }
}
