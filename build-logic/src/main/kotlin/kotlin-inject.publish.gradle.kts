import java.io.FileInputStream
import java.net.URI
import java.util.Locale
import java.util.Properties

plugins {
    `maven-publish`
    signing
}

version = rootProject.version

fun MavenPublication.mavenCentralPom() {
    pom {
        name.set("kotlin-inject-viewmodel-factory")
        description.set("A compile-time viewmodel factory generator for kotlin-inject")
        url.set("https://github.com/NicolaVerbeeck/kotlin-inject-viewmodel-factory")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("NicolaVerbeeck")
                name.set("Nicola Verbeeck")
            }
        }
        scm {
            connection.set("https://github.com/NicolaVerbeeck/kotlin-inject-viewmodel-factory.git")
            developerConnection.set("https://github.com/NicolaVerbeeck/kotlin-inject-viewmodel-factory.git")
            url.set("https://github.com/NicolaVerbeeck/kotlin-inject-viewmodel-factory")
        }
    }
}

publishing {
    if (plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
        // already has publications, just need to add javadoc task
        val javadocJar by tasks.creating(Jar::class) {
            from("javadoc")
            archiveClassifier.set("javadoc")
        }
        publications.all {
            if (this is MavenPublication) {
                groupId = rootProject.group.toString()
                // We want the artifactId to represent the full project path
                artifactId = path
                    .trimStart(':')
                    .replace(":", "-")
                artifact(javadocJar)
                mavenCentralPom()
            }
        }
        // create task to publish all apple (macos, ios, tvos, watchos) artifacts
        @Suppress("UNUSED_VARIABLE")
        val publishApple by tasks.registering {
            publications.all {
                if (name.contains(Regex("macos|ios|tvos|watchos"))) {
                    dependsOn("publish${name.capitalize(Locale.ROOT)}PublicationToSonatypeRepository")
                }
            }
        }
    } else {
        // Need to create source, javadoc & publication
        val java = extensions.getByType<JavaPluginExtension>()
        java.withSourcesJar()
        java.withJavadocJar()
        publications {
            create<MavenPublication>("lib") {
                from(components["java"])
                mavenCentralPom()
                groupId = rootProject.group.toString()
                // We want the artifactId to represent the full project path
                artifactId = path
                    .trimStart(':')
                    .replace(":", "-")
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            url = URI.create("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = findProperty("ossrhUsername") as String?
                password = findProperty("ossrhPassword") as String?
            }
        }
    }
}

signing {
    setRequired {
        findProperty("signing.keyId") != null
    }

    publishing.publications.all {
        sign(this)
    }
}