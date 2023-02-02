
// Find all projects except for the root project and this project.
val disallowed = setOf(project.name, project.parent!!.name)
val includedProjects = project.parent?.allprojects?.filter { !disallowed.contains(it.name) } ?: emptyList()

dependencies {
    includedProjects.forEach {
        api(project(":" + it.name))
    }
}

tasks {
    withType(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class.java) {
        minimize {
            includedProjects.forEach {
                exclude(project(":" + it.name))
            }
        }
        manifest {
            attributes(
                "Main-Class" to "net.minestom.vanilla.server.VanillaServer",
                "Multi-Release" to true
            )
        }
        mergeServiceFiles()
    }
}