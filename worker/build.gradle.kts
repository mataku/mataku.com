import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.ir.Executable

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

repositories {
    mavenCentral()
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
        binaries.executable()

        compilations.configureEach {
            binaries.withType<Executable>().configureEach {
                linkTask.configure {
                    val moduleName = linkTask.flatMap { it.compilerOptions.moduleName }
                    val fileName = moduleName.map { "$it.uninstantiated.mjs" }
                    val mjsFile = linkTask.flatMap { it.destinationDirectory.file(fileName.get()) }

                    doLast {
                        val module = moduleName.get()
                        val file = mjsFile.get().asFile
                        val text = file.readText()
                        val newText = text
                            .replace(
                                "if (!isNodeJs && !isDeno && !isStandaloneJsVM && !isBrowser) {\n      throw \"Supported JS engine not detected\";\n    }",
                                ""
                            )
                            .replace(
                                Regex("""if \(isNodeJs\) \{.+?\n      \}""", RegexOption.DOT_MATCHES_ALL),
                                ""
                            )
                            .replace(
                                "if (isBrowser) {",
                                """
                                |const isCloudflareWorker = typeof navigator !== 'undefined' && navigator.userAgent.includes("Cloudflare-Workers");
                                |      if (isCloudflareWorker) {
                                |        const { default: wasmModule } = await import('./$module.wasm');
                                |        wasmInstance = (await WebAssembly.instantiate(wasmModule, importObject));
                                |      }
                                |
                                |      else if (isBrowser) {
                                """.trimMargin()
                            )
                        file.writeText(newText)
                    }
                }
            }
        }
    }

    sourceSets {
        wasmJsMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-browser:0.3")
            }
        }
    }
}
