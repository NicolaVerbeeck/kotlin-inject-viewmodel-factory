plugins {
    kotlin("jvm") version "1.6.21" apply false
}

group = "com.chimerapps.kotlin-inject-viewmodelfactory"
version = "0.1.0"

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.6.21"))
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}