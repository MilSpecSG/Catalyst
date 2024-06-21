plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.anvil.velocity)

    compileOnly(platform(libs.adventure.bom))
    compileOnly("net.kyori:adventure-text-serializer-legacy")
    compileOnly("net.kyori:adventure-text-serializer-plain")

    implementation(project(":catalyst-api"))
    implementation(project(":catalyst-core"))
}
