plugins {
    kotlin("kapt")
}

dependencies {
    api(project(":catalyst-app-breeze-core"))
    api(project(":catalyst-velocity"))
    api(project(":catalyst-core"))
    api(project(":catalyst-core-chat"))
    api(project(":catalyst-core-command"))
    kapt(libs.velocity)
}
