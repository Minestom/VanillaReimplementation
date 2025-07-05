dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":datapack-loading"))
    implementation("net.goldenstack:window:${project.property("window_version")}")
}