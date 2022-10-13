dependencies {
    implementation(project(":block-update-system"))
    implementation(project(":blocks"))
    implementation(project(":commands"))
    implementation(project(":core"))
    implementation(project(":crafting-data"))
    implementation(project(":entities"))
    implementation(project(":entity-meta"))
    implementation(project(":instance-meta"))
    implementation(project(":fluid-simulation"))
    implementation(project(":item-placeables"))
    implementation(project(":world-generation"))
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