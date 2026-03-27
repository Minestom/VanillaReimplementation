plugins {
    java
    `java-library`
    `maven-publish`
    id("com.github.harbby.gradle.serviceloader") version ("1.1.9")
    id("com.gradleup.shadow") version "8.3.10" apply false
}

subprojects {
    plugins.apply("java")
    plugins.apply("java-library")
    plugins.apply("maven-publish")
    plugins.apply("com.github.harbby.gradle.serviceloader")
    plugins.apply("com.gradleup.shadow")

    group = "net.minestom.vanilla"
    version = "indev"

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25

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

    tasks.withType<JavaCompile>().configureEach {
        options.release = 25
    }

    tasks.withType<Test>().configureEach {
        javaLauncher = javaToolchains.launcherFor {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    tasks.withType<JavaExec>().configureEach {
        javaLauncher = javaToolchains.launcherFor {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    tasks.withType<Javadoc>().configureEach {
        javadocTool = javaToolchains.javadocToolFor {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
        mavenLocal()
    }

    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.13.4"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
