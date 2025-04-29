plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "ru.danl"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
}

