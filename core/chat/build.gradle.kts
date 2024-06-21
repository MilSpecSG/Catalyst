dependencies {
    api(project(":catalyst-core"))
    implementation(libs.discord.jda) {
        exclude(group = "org.slf4j")
    }
    implementation(libs.discord.webhooks) {
        exclude(group = "org.slf4j")
    }
}
