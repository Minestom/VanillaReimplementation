dependencies {

    // Minestom
    api("com.github.Minestom:Minestom:${project.property("minestom_version")}")

    // Raycasting
    api("com.github.EmortalMC:Rayfast:${project.property("rayfast_version")}")

    // Noise
    api("com.github.Articdive:JNoise:${project.property("jnoise_version")}")

    // Annotations
    api("org.jetbrains:annotations:${project.property("annotations_version")}")

    // Tinylog for colored logging
    implementation("org.tinylog:tinylog-api:2.5.0")
    implementation("org.tinylog:tinylog-impl:2.5.0")

    // jansi for colored console
    implementation("org.fusesource.jansi:jansi:2.4.0")

}