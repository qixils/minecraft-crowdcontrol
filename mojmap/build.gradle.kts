val mojmapVersion: String by project
val loaderVersion: String by project
val configurateVersion: String by project
val adventurePlatformModVersion: String by project
val clothConfigVersion: String by project
val adventureVersion: String by project
val adventurePlatformVersion: String by project
val cloudMojmapVersion: String by project
val fabricVersion: String by project

plugins {
    id("architectury-plugin")
    id("dev.architectury.loom")
}

architectury {
    common("fabric", "neoforge")
}

dependencies {
    implementation(project(":configurate-common"))
    implementation("org.spongepowered:configurate-hocon:$configurateVersion")

    minecraft("net.minecraft:minecraft:$mojmapVersion")
    mappings(loom.officialMojangMappings())

    // We depend on Fabric Loader here to use the Fabric @Environment annotations,
    // which get remapped to the correct annotations on each platform.
    // Do NOT use other classes from Fabric Loader.
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    modCompileOnly("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    modCompileOnly("net.kyori:adventure-platform-mod-shared-fabric-repack:$adventurePlatformModVersion")
    modCompileOnly("org.incendo:cloud-minecraft-modded-common-fabric-repack:$cloudMojmapVersion")
    modCompileOnly("me.shedaniel.cloth:cloth-config:$clothConfigVersion")

    // TODO: adventure is being weird
    compileOnly("net.kyori:adventure-api:$adventureVersion")
    compileOnly("net.kyori:adventure-platform-api:$adventurePlatformVersion")
}

loom {
//    mixin {
//        defaultRefmapName.set("crowd-control-refmap.json")
//    }
    accessWidenerPath = file("src/main/resources/crowdcontrol.accesswidener")
}
