dependencies {
    compileOnly(project(":core"))
    compileOnly(project(":datapack"))
    implementation("net.goldenstack:window:${project.property("window_version")}")
}