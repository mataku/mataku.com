package blog

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes

object AssetHasher {
  fun processStylesCss(projectRoot: Path): String = processAsset(projectRoot, "templates/styles.css", "styles", "css")

  fun processThemeJs(projectRoot: Path): String = processAsset(projectRoot, "templates/theme.js", "theme", "js")

  private fun processAsset(
    projectRoot: Path,
    sourcePath: String,
    baseName: String,
    extension: String,
  ): String {
    val source = projectRoot.resolve(sourcePath)
    val outputAssetsDir = projectRoot.resolve("output/assets")
    val hash = computeHash(source)
    val hashedName = "$baseName.$hash.$extension"
    val target = outputAssetsDir.resolve(hashedName)

    outputAssetsDir
      .listDirectoryEntries("$baseName.*.$extension")
      .forEach { it.deleteIfExists() }

    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)

    return hashedName
  }

  private fun computeHash(
    filePath: Path,
    length: Int = 8,
  ): String {
    val bytes = filePath.readBytes()
    val digest = MessageDigest.getInstance("MD5").digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }.take(length)
  }
}
