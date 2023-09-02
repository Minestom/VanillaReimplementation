rootProject.name = "VanillaReimplementation"
include("core")
include("world-generation")
include("commands")
include("instance-meta")
include("block-update-system")
include("fluid-simulation")
include("item-placeables")
include("blocks")
include("entities")
include("entity-meta")
include("server")
include("items")
include("mojang-data")
include("datapack-loading")
include("datapack-tests")

pluginManagement {
    repositories {
        mavenCentral()
        maven("https://repo.spongepowered.org/repository/maven-public")
        maven("https://repo.spongepowered.org/repository/maven-snapshots")
    }
}