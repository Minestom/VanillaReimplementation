plugins {
    java
    id ("com.github.harbby.gradle.serviceloader") version ("1.1.8")
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

subprojects {

    plugins.apply("java")
    plugins.apply("java-library")
    plugins.apply("maven-publish")
    plugins.apply("com.github.harbby.gradle.serviceloader")

    group = "net.minestom.vanilla"
    version = "indev"


    // Specify java version
    java.sourceCompatibility = JavaVersion.VERSION_17
    java.targetCompatibility = JavaVersion.VERSION_17

    java {
        withJavadocJar()
        withSourcesJar()
    }

    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
    }

    serviceLoader.serviceInterfaces.add("net.minestom.vanilla.VanillaReimplementation\$Feature")
}