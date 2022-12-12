dependencies {
    // Add all projects except for the root project and this project.
    val disallowed = setOf(project.name, project.parent!!.name)
    project.parent?.allprojects?.forEach {
        if (disallowed.contains(it.name)) return@forEach
        api(project(":" + it.name))
    }
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        manifest {
            attributes(
                "Main-Class" to "net.minestom.vanilla.server.VanillaServer",
                "Multi-Release" to true
            )
        }
        mergeServiceFiles()
    }
}