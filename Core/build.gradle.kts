plugins {
    java
    `java-library`
}

group = "net.minestom.vanilla"
version = "1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {

    // Minestom
    api("com.github.Minestom:Minestom:${project.property("minestom_version")}")

    // Raycasting
    api("com.github.EmortalMC:Rayfast:${project.property("rayfast_version")}")

    // Noise
    api("com.github.Articdive:JNoise:${project.property("jnoise_version")}")

    // Annotations
    api("org.jetbrains:annotations:${project.property("annotations_version")}")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}