plugins {
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

dependencies {
    compileOnly(project(":core"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(project(":core"))
    testImplementation(project(":datapack-loading"))
}

tasks.test {
    useJUnitPlatform()
}

minecraft {
    version("1.20.4")
    runs {
        server()
    }
}