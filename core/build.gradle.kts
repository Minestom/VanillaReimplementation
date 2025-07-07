dependencies {
    // Minestom
    api("net.minestom:minestom:${project.property("minestom_version")}")

    // Raycasting
    api("com.github.EmortalMC:Rayfast:${project.property("rayfast_version")}")

    // Noise
    api("com.github.Articdive:JNoise:${project.property("jnoise_version")}")

    // Annotations
    api("org.jetbrains:annotations:${project.property("annotations_version")}")

    // SLF4j
    api("org.slf4j:slf4j-api:${project.property("slf4j_version")}")

    // Json
    api("com.squareup.moshi:moshi:1.14.0")
    api("com.squareup.moshi:moshi-adapters:1.14.0")

    // Tests
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
