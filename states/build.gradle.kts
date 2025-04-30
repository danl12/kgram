plugins {
    kotlin("jvm")
//    `maven-publish`
}

group = "com.danl.kgram"
version = "0.1.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
}
//
//publishing {
//    publications {
//        create<MavenPublication>("maven") {
//            groupId = "com.danl.kgram"
//            artifactId = "kgram-states"
//            version = "0.1.1"
//
//            from(components["java"])
//        }
//    }
//}

