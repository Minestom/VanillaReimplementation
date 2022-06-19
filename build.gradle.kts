plugins {
    java
    `java-library`
    `maven-publish`
    id("com.github.harbby.gradle.serviceloader") version("1.1.8")
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withJavadocJar()
    withSourcesJar()

    sourceSets.main {
        java.srcDir("src/main/java")
    }
}

dependencies {
    implementation(project(":block-update-system"))
    implementation(project(":commands"))
    implementation(project(":core"))
    implementation(project(":entities"))
    implementation(project(":entity-meta"))
    implementation(project(":instance-meta"))
    implementation(project(":vanilla-blocks"))
    implementation(project(":world-generation"))
}

subprojects {

    plugins.apply("java")
    plugins.apply("java-library")
    plugins.apply("maven-publish")
    plugins.apply("com.github.harbby.gradle.serviceloader")

    group = "net.minestom.vanilla"
    version = "indev"

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withJavadocJar()
        withSourcesJar()

        sourceSets.main {
            java.srcDir("src/main/java")
        }
    }

    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
    }

    serviceLoader.serviceInterfaces.add("net.minestom.vanilla.VanillaReimplementation\$Feature")
}