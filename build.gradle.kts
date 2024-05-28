plugins {
    java
    `java-library`
    `maven-publish`
    id("com.github.harbby.gradle.serviceloader") version ("1.1.8")
    id("com.github.johnrengelman.shadow") version ("7.1.2")
}

subprojects {

    plugins.apply("java")
    plugins.apply("java-library")
    plugins.apply("maven-publish")
    plugins.apply("com.github.harbby.gradle.serviceloader")
    plugins.apply("com.github.johnrengelman.shadow")

    group = "net.minestom.vanilla"
    version = "indev"

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21

//        withJavadocJar()
        withSourcesJar()

        sourceSets.main {
            java.srcDir("src/main/java")
        }
    }

    tasks.withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    tasks.withType<Wrapper> {
        gradleVersion = rootProject.gradle.gradleVersion
    }

    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
        mavenLocal()
    }

    dependencies {
    }

    publishing {
        publications {
            register("maven", MavenPublication::class) {
                from(components["java"])
            }
        }
    }

    serviceLoader.serviceInterfaces.add("net.minestom.vanilla.VanillaReimplementation\$Feature")
    serviceLoader.serviceInterfaces.add("org.slf4j.spi.SLF4JServiceProvider")

    tasks.getByName("build").dependsOn("shadowJar")

    tasks.withType<Test> {
        dependsOn("serviceLoaderBuild")
        useJUnitPlatform()
    }
}