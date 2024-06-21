import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
    alias(libs.plugins.kotlin.serialization)
}

val projectVersion: String = run {
    val rawVersion = file("version").readLines().first()
    if (project.hasProperty("rawVersion")) {
        rawVersion
    } else {
        val branch = System.getenv("VCS_BRANCH")?.replace('/', '-') ?: "unknown-branch"
        System.getenv("BUILD_NUMBER")?.let { buildNumber ->
            val gitRev = ByteArrayOutputStream()
            exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
                standardOutput = gitRev
            }.assertNormalExitValue()
            rawVersion.replace("SNAPSHOT", "BETA$buildNumber-$branch-${gitRev.toString().trim()}")
        } ?: rawVersion
    }
}

logger.warn("Resolved project version $projectVersion")

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "java-library")

    group = "org.anvilpowered"
    version = projectVersion

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
            freeCompilerArgs = listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xcontext-receivers",
            )
        }
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    shadowJar {

        mergeServiceFiles()

        exclude("org/intellij/**")
        exclude("org/slf4j/**")
        exclude("kotlin/**") // external kotlin dependency

        relocate("org.anvilpowered.anvil", "org.anvilpowered.catalyst.relocated.anvil")
        relocate("org.anvilpowered.kbrig", "org.anvilpowered.catalyst.relocated.kbrig")
        relocate("org.jetbrains", "org.anvilpowered.catalyst.relocated.jetbrains")
        relocate("kotlinx", "org.anvilpowered.catalyst.relocated.kotlinx")
        relocate("org.koin", "org.anvilpowered.catalyst.relocated.koin")
        relocate("co.touchlab.stately", "org.anvilpowered.catalyst.relocated.stately")

        relocate("org.mariadb", "org.anvilpowered.catalyst.relocated.mariadb")
        relocate("org.checkerframework", "org.anvilpowered.catalyst.relocated.checkerframework")
        relocate("waffle", "org.anvilpowered.catalyst.relocated.waffle")
        relocate("com.github.benmanes.caffeine", "org.anvilpowered.catalyst.relocated.caffeine")
        relocate("com.google.errorprone", "org.anvilpowered.catalyst.relocated.errorprone")
        relocate("com.sun.jna", "org.anvilpowered.catalyst.relocated.jna")

        relocate("net.dv8tion", "org.anvilpowered.catalyst.relocated.dv8tion")
        relocate("com.fasterxml.jackson", "org.anvilpowered.catalyst.relocated.jackson")
        relocate("org.json", "org.anvilpowered.catalyst.relocated.json")
        relocate("net.sf.trove4j", "org.anvilpowered.catalyst.relocated.trove4j")
        relocate("com.neovisionaries", "org.anvilpowered.catalyst.relocated.neovisionaries")
        relocate("okhttp3", "org.anvilpowered.catalyst.relocated.okhttp3")
        relocate("okio", "org.anvilpowered.catalyst.relocated.okio")
        relocate("gnu.trove", "org.anvilpowered.catalyst.relocated.trove")
        relocate("club.minnced", "org.anvilpowered.catalyst.relocated.minnced")
        relocate("tomp2p", "org.anvilpowered.catalyst.relocated.tomp2p")
        relocate("com.iwebpp", "org.anvilpowered.catalyst.relocated.iwebpp")
        relocate("org.apache.commons", "org.anvilpowered.catalyst.relocated.apache.commons")

        relocate("org.postgresql", "org.anvilpowered.catalyst.relocated.postgresql")
        relocate("org.spongepowered", "org.anvilpowered.catalyst.relocated.spongepowered")
        relocate("io.leangen.geantyref", "org.anvilpowered.catalyst.relocated.geantyref")
    }
}
