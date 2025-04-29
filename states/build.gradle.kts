plugins {
    kotlin("jvm")
}

group = "ru.danl"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
}

