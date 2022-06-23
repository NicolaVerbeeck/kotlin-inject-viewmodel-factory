plugins {
    kotlin("jvm")
    id("kotlin-inject.publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.21-1.0.5")
    implementation("com.squareup:kotlinpoet:1.12.0")
}
