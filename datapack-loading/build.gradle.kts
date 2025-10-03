dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":mojang-data"))
    implementation("space.vectrix.flare:flare:2.0.1")
    implementation("space.vectrix.flare:flare-fastutil:2.0.1")
    
    // Test dependencies
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(project(":core"))
    testImplementation(project(":mojang-data"))
}

tasks.test {
    useJUnitPlatform()
}