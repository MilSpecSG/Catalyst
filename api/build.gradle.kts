plugins {
    id("catalyst-publish")
}

dependencies {
    compileOnlyApi(libs.luckperms)
    compileOnlyApi(platform(libs.adventure.bom))
    compileOnlyApi("net.kyori:adventure-text-serializer-legacy")
    compileOnlyApi("net.kyori:adventure-text-serializer-plain")
    compileOnlyApi("net.kyori:adventure-text-minimessage")
    api(libs.anvil.core)
    api(libs.kotlinx.serialization)
}
