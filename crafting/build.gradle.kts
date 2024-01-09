dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":datapack-loading"))
    implementation("com.github.GoldenStack:window:${project.property("window_version")}")
}