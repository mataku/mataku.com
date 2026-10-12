package blog

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.relativeTo
import kotlin.streams.asSequence

/**
 * Mirrors hand-maintained files into the output directory, copying only
 * files that are missing or stale so repeated runs are cheap.
 */
object StaticFileSyncer {
  fun sync(
    sourceRoot: Path,
    targetRoot: Path,
    include: (Path) -> Boolean = { true },
  ): Int {
    if (!sourceRoot.isDirectory()) return 0

    var copied = 0
    Files.walk(sourceRoot).use { paths ->
      paths
        .asSequence()
        .filter { it.isRegularFile() && include(it) }
        .forEach { source ->
          val target = targetRoot.resolve(source.relativeTo(sourceRoot))
          if (isUpToDate(source, target)) return@forEach
          target.parent.createDirectories()
          Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
          copied++
        }
    }
    return copied
  }

  private fun isUpToDate(
    source: Path,
    target: Path,
  ): Boolean =
    target.exists() &&
      source.fileSize() == target.fileSize() &&
      source.getLastModifiedTime() <= target.getLastModifiedTime()
}
