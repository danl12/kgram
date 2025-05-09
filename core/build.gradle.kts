plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.github.danl12"
version = "0.1.1"

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    api("org.telegram:telegrambots-meta:8.3.0")
    implementation("org.telegram:telegrambots-longpolling:8.3.0")
    implementation("org.telegram:telegrambots-client:8.3.0")
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}