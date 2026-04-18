package blog

import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AssetHasherTest {
  @TempDir
  lateinit var projectRoot: Path

  private fun setUpTemplate(
    relativePath: String,
    content: String,
  ): Path {
    val file = projectRoot.resolve(relativePath)
    file.parent.createDirectories()
    file.writeText(content)
    return file
  }

  private fun assetsDir(): Path = projectRoot.resolve("output/assets").also { it.createDirectories() }

  @Test
  fun `returns hashed file name derived from content`() {
    setUpTemplate("templates/styles.css", "hello")
    assetsDir()

    val name = AssetHasher.processStylesCss(projectRoot)

    // MD5("hello") = 5d41402abc4b2a76b9719d911017c592 -> first 8 chars = 5d41402a
    assertEquals("styles.5d41402a.css", name)
  }

  @Test
  fun `copies source file to hashed path in output assets`() {
    setUpTemplate("templates/styles.css", "hello")
    assetsDir()

    val name = AssetHasher.processStylesCss(projectRoot)
    val target = projectRoot.resolve("output/assets").resolve(name)

    assertTrue(target.exists())
    assertEquals("hello", target.readText())
  }

  @Test
  fun `removes previous hashed variants of the same asset`() {
    setUpTemplate("templates/styles.css", "hello")
    val assets = assetsDir()
    val stale = assets.resolve("styles.oldhash1.css").also { it.writeText("stale") }
    val unrelated = assets.resolve("theme.abcd1234.js").also { it.writeText("keep") }

    AssetHasher.processStylesCss(projectRoot)

    assertFalse(stale.exists())
    assertTrue(unrelated.exists(), "unrelated asset families should not be deleted")
  }

  @Test
  fun `produces theme js name based on theme js content`() {
    setUpTemplate("templates/theme.js", "hello")
    assetsDir()

    val name = AssetHasher.processThemeJs(projectRoot)

    assertEquals("theme.5d41402a.js", name)
  }

  @Test
  fun `hash changes when source content changes`() {
    val source = setUpTemplate("templates/styles.css", "hello")
    assetsDir()
    val first = AssetHasher.processStylesCss(projectRoot)

    Files.writeString(source, "world")
    val second = AssetHasher.processStylesCss(projectRoot)

    assertTrue(first != second)
    assertTrue(projectRoot.resolve("output/assets").resolve(second).exists())
    assertFalse(projectRoot.resolve("output/assets").resolve(first).exists())
  }
}
