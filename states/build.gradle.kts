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

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "ru.danl.kgram"
            artifactId = "states"
            version = "0.1"

            from(components["java"])
        }
    }
}

