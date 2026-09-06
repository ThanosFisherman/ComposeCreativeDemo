import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.*

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
tasks.register<org.gradle.jvm.tasks.Jar>("fatJarAllPlatforms") {

    description = "FatJarAllPlatforms"

    // sets the name of the .jar file this produces to the name of the game or app, with the version after.
    archiveFileName.set("${project.name}-${project.version}.jar")
    // the duplicatesStrategy matters starting in Gradle 7.0; this setting works.
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val runtimeClasspath = configurations.getByName("runtimeClasspath")
    dependsOn(runtimeClasspath)

    from({
        val dirsPath = runtimeClasspath.map { file ->
            if (file.isDirectory) file else zipTree(file)
        }
        val kotlinMainPath = kotlin.target.compilations.getByName("main").output
        dirsPath + kotlinMainPath
    })

    // these "exclude" lines remove some unnecessary duplicate files in the output JAR.
    exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    // setting the manifest makes the JAR runnable.
    manifest {
        attributes["Main-Class"] = "io.github.thanosfisherman.demo.MainKt"
    }
    // this last step may help on some OSes that need extra instruction to make runnable JARs.
    doLast {
        file(archiveFile).setExecutable(true, false)
    }
}

dependencies {
    implementation(project(":shared"))
    //implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.desktop:desktop-jvm-windows-x64:1.12.0")
    implementation("org.jetbrains.compose.desktop:desktop-jvm-macos-x64:1.12.0")
    implementation("org.jetbrains.compose.desktop:desktop-jvm-macos-arm64:1.12.0")
    implementation("org.jetbrains.compose.desktop:desktop-jvm-linux-x64:1.12.0")
    implementation("org.jetbrains.compose.desktop:desktop-jvm-linux-arm64:1.12.0")
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.compose.material3)
    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "io.github.thanosfisherman.demo.MainKt"
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        if (os.contains("mac"))
            jvmArgs += listOf("-XstartOnFirstThread")
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.github.thanosfisherman.demo"
            packageVersion = "1.0.0"
        }
    }
}
