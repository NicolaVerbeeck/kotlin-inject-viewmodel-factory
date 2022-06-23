plugins {
    kotlin("multiplatform")
    id("kotlin-inject.publish")
}

val nativeTargets = arrayOf(
    "linuxX64",
    "macosX64", "macosArm64",
    "iosArm32", "iosArm64", "iosX64", "iosSimulatorArm64",
    "tvosArm64", "tvosX64", "tvosSimulatorArm64",
    "watchosArm32", "watchosArm64", "watchosX86", "watchosX64", "watchosSimulatorArm64",
)

kotlin {

    jvm()

    for (target in nativeTargets) {
        targets.add(presets.getByName(target).createTarget(target))
    }

    sourceSets {
    }
}
