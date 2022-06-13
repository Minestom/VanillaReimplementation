plugins {
    id ("com.github.harbby.gradle.serviceloader") version ("1.1.8")
}

group = "net.minestom.vanilla"
version = "1.0"

subprojects {

    plugins.apply("java")
    plugins.apply("java-library")
    plugins.apply("com.github.harbby.gradle.serviceloader")

    group = "net.minestom.vanilla"
    version = "indev"

    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
    }


    serviceLoader.serviceInterfaces.add("net.minestom.vanilla.VanillaReimplementation\$Feature")
}