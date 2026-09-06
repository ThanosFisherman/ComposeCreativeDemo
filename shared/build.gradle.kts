import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

compose.resources {
    publicResClass = true
}

val lwjglVersion = "3.4.3"
val lwjglNatives = listOf(
    "natives-freebsd",
    "natives-linux",
    "natives-linux-arm32",
    "natives-linux-arm64",
    "natives-linux-ppc64le",
    "natives-linux-riscv64",
    "natives-macos",
    "natives-macos-arm64",
    "natives-windows",
    "natives-windows-arm64",
    "natives-windows-x86"
)

kotlin {
    jvm()

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    android {
        namespace = "io.github.thanosfisherman.demo.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jsMain.dependencies {
            implementation(libs.wrappers.browser)
        }
        jvmMain.dependencies {
            implementation(dependencies.platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
            implementation("org.lwjgl:lwjgl")
            implementation("org.lwjgl:lwjgl-openal")
            implementation("org.lwjgl:lwjgl-stb")
            lwjglNatives.forEach { native ->
                implementation("org.lwjgl:lwjgl::$native")
                implementation("org.lwjgl:lwjgl-openal::$native")
                implementation("org.lwjgl:lwjgl-stb::$native")
            }
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}