plugins {
    kotlin("jvm")
    `maven-publish`
}

version = "0.1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
}

