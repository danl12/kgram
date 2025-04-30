plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.github.danl.kgram"
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
            groupId = "com.github.danl.kgram"
            artifactId = "states"
            version = "0.1"

            from(components["java"])
        }
    }
}

