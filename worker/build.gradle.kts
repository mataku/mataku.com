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
                    val fileName = moduleName.map { "$it.mjs" }
                    val mjsFile = linkTask.flatMap { it.destinationDirectory.file(fileName.get()) }

                    // Patch the generated .mjs for Cloudflare Workers compatibility.
                    // The Kotlin/Wasm compiler generates environment-specific WASM instantiation code
                    // (Node.js, Deno, browser, etc.), but does not support Cloudflare Workers natively.
                    // These replacements make the generated code runnable on Cloudflare Workers.
                    // See: https://youtrack.jetbrains.com/issue/KT-65796
                    //      https://youtrack.jetbrains.com/issue/KT-73159
                    doLast {
                        val module = moduleName.get()
                        val file = mjsFile.get().asFile
                        val text = file.readText()
                        val newText = text
                            // Remove the "Supported JS engine not detected" guard since
                            // Cloudflare Workers is not recognized as a known environment.
                            .replace(
                                "if (!isNodeJs && !isDeno && !isStandaloneJsVM && !isBrowser) {\n  throw \"Supported JS engine not detected\";\n}",
                                ""
                            )
                            // Remove the isNodeJs block to avoid importing 'node:module',
                            // which causes a wrangler bundling warning.
                            .replace(
                                Regex("""if \(isNodeJs\) \{.+?\n  \}""", RegexOption.DOT_MATCHES_ALL),
                                ""
                            )
                            // Insert Cloudflare Workers WASM instantiation before the isBrowser branch.
                            // Cloudflare Workers defines `self`, making isBrowser true, so we use
                            // `else if` to skip the browser path when the Cloudflare Workers path is taken.
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

                        // Patch the import-object.mjs to remove Node.js detection code
                        // that imports 'node:module', which causes a wrangler bundling warning.
                        val importObjectFile = mjsFile.get().asFile.parentFile
                            .resolve("$module.import-object.mjs")
                        if (importObjectFile.exists()) {
                            val importObjectText = importObjectFile.readText()
                            val patchedImportObjectText = importObjectText.replace(
                                Regex("""if \(typeof process !== 'undefined' && process\.release\.name === 'node'\) \{.+?\}""", RegexOption.DOT_MATCHES_ALL),
                                ""
                            )
                            importObjectFile.writeText(patchedImportObjectText)
                        }
                    }
                }
            }
        }
    }

    sourceSets {
        wasmJsMain {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
    }
}
