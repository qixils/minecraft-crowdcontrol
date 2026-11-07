val configurateVersion = project.property("configurateVersion") as String
val adventurePlatformFabricVersion = project.property("adventurePlatformFabricVersion") as String
val adventureVersion = project.property("adventureVersion") as String
val yaclVersion = project.property("yaclVersion") as String
val minecraft_version = project.property("minecraft_version") as String
val mod_id = project.property("mod_id") as String

plugins {
    id("multiloader-common")
    id("net.fabricmc.fabric-loom-remap")
}

loom {
    accessWidenerPath.set(project(":fabric-platform").file("src/main/resources/$mod_id.accesswidener"))
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.officialMojangMappings())

    // multiloader common
    compileOnly("org.spongepowered:mixin:0.8.5")
    compileOnly("org.ow2.asm:asm:9.6")
    compileOnly("io.github.llamalad7:mixinextras-common:0.5.3")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.3")

    //
    api(project(":base-common"))
    api("org.spongepowered:configurate-hocon:$configurateVersion")

    modCompileOnly("net.kyori:adventure-platform-fabric:$adventurePlatformFabricVersion")
    modCompileOnly("maven.modrinth:yacl:$yaclVersion")
}

val commonJava = configurations.create("commonJava") {
    isCanBeResolved = false
    isCanBeConsumed = true
}

val commonResources = configurations.create("commonResources") {
    isCanBeResolved = false
    isCanBeConsumed = true
}

artifacts {
    add("commonJava", sourceSets["main"].java.srcDirs.single())
    add("commonResources", sourceSets["main"].resources.srcDirs.single())
}

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
listOf("apiElements", "runtimeElements"/*, "sourcesElements", "javadocElements"*/).forEach { variant ->
    configurations.named(variant) {
        attributes.attribute(loaderAttribute, "common")
    }
}

sourceSets.configureEach {
    listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName).forEach { variant ->
        configurations.named(variant) {
            attributes.attribute(loaderAttribute, "common")
        }
    }
}
