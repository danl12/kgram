plugins {
    kotlin("jvm")
}

group = "ru.danl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.telegram:telegrambots-longpolling:8.3.0")
    implementation("org.telegram:telegrambots-client:8.3.0")
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}