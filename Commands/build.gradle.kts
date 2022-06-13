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
    implementation(project(":InstanceMeta"))
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
//
//tasks.getByName<Jar>("jar") {
//    from("./src/main/java") {
//        include("META-INF/services/net.minestom.vanilla.commands.VanillaCommandsFeature")
//    }
//}