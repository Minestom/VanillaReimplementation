plugins {
    id ("com.github.harbby.gradle.serviceloader") version ("1.1.8")
}
group = "net.minestom.vanilla"
version = "1.0"

subprojects {

    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")

    }

    plugins.apply("com.github.harbby.gradle.serviceloader")

    serviceLoader.serviceInterfaces.add("net.minestom.vanilla.VanillaReimplementation\$Feature")
}