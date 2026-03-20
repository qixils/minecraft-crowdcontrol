val mojmapVersion: String by project
val loaderVersion: String by project
val configurateVersion: String by project
val adventurePlatformModVersion: String by project
val clothConfigVersion: String by project
val adventureVersion: String by project
val adventurePlatformVersion: String by project
val cloudVersion: String by project
val fabricVersion: String by project

plugins {
    id("architectury-plugin")
    id("dev.architectury.loom")
}

architectury {
    common("fabric")//, "neoforge")
}

dependencies {
    implementation(project(":base-common"))
    implementation("org.spongepowered:configurate-hocon:$configurateVersion")

    minecraft("net.minecraft:minecraft:$mojmapVersion")
    mappings(loom.officialMojangMappings())

    // We depend on Fabric Loader here to use the Fabric @Environment annotations,
    // which get remapped to the correct annotations on each platform.
    // Do NOT use other classes from Fabric Loader.
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    modCompileOnly("net.kyori:adventure-platform-fabric:$adventurePlatformModVersion") {
        exclude(group = "net.kyori", module = "adventure-api")
    }
    modCompileOnly("dev.qixils.cloud:cloud-fabric:$cloudVersion")
    modCompileOnly("maven.modrinth:cloth-config:$clothConfigVersion")

    // TODO: adventure is being weird
    compileOnly("net.kyori:adventure-api:$adventureVersion")
    compileOnly("net.kyori:adventure-platform-api:$adventurePlatformVersion") {
        exclude(group = "net.kyori", module = "adventure-api")
    }
}

loom {
//    mixin {
//        useLegacyMixinAp.set(true)
//        defaultRefmapName.set("crowdcontrol-refmap.json")
//    }
    accessWidenerPath = file("src/main/resources/crowdcontrol.accesswidener")
}
