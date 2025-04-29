plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(project(":states"))
    implementation("org.telegram:telegrambots-meta:8.3.0")
}

kotlin {
    jvmToolchain(21)
}