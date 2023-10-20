dependencies {
    // Minestom
    api("dev.hollowcube:minestom-ce:${project.property("minestom_version")}")

    // Raycasting
    api("com.github.EmortalMC:Rayfast:${project.property("rayfast_version")}")

    // Noise
    api("com.github.Articdive:JNoise:${project.property("jnoise_version")}")

    // Annotations
    api("org.jetbrains:annotations:${project.property("annotations_version")}")

    // Json
    api("com.squareup.moshi:moshi:1.14.0")
    api("com.squareup.moshi:moshi-adapters:1.14.0")
}