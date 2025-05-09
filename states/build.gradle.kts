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
    implementation(project(":core"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

