@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        mavenCentral()
        maven("https://libraries.minecraft.net")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "catalyst"

sequenceOf(
    "app-breeze",
    "app-breeze-backend",
    "app-breeze-core",
    "app-breeze-proxy",
    "app-cyclone",
    "app-cyclone-backend",
    "app-cyclone-core",
    "app-cyclone-proxy",
    "app-cyclone-worker",
    "app-droplet",
    "app-droplet-core",
    "app-droplet-paper",
    "app-droplet-sponge",
    "api",
    "core",
    "core-chat",
    "core-command",
    "velocity",
    "velocity-listener",
).forEach {
    val project = ":catalyst-$it"
    include(project)
    project(project).projectDir = file(it.replace('-', '/'))
}
