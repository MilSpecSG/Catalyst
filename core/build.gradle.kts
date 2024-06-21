dependencies {
    api(project(":catalyst-api"))
    runtimeOnly(libs.driver.mariadb) {
        exclude(group = "org.slf4j")
    }
    runtimeOnly(libs.driver.postgresql)

    api(platform(libs.exposed.bom))
    api(libs.bundles.exposed) {
        exclude(group = "org.slf4j")
    }
}
