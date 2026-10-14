val mojmapVersion = project.property("mojmapVersion") as String
val loaderVersion = project.property("loaderVersion") as String
val configurateVersion = project.property("configurateVersion") as String
val adventurePlatformModVersion = project.property("adventurePlatformModVersion") as String
val clothConfigVersion = project.property("clothConfigVersion") as String
val adventureVersion = project.property("adventureVersion") as String
val adventurePlatformVersion = project.property("adventurePlatformVersion") as String
val cloudVersion = project.property("cloudVersion") as String
val fabricVersion = project.property("fabricVersion") as String

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

// shaded jar is never consumed directly we don't need it, this just shades all of minecraft
tasks.shadowJar {
    enabled = false
}
