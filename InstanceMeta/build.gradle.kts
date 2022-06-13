plugins {
    java
}

version = "unspecified"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(project(":Core"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}