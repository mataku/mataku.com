import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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
  }

  sourceSets {
    wasmJsMain {
      dependencies {
        implementation(libs.kotlinx.browser)
      }
    }
    wasmJsTest {
      dependencies {
        implementation(kotlin("test"))
      }
    }
  }
}
