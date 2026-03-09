val configurateVersion: String by project
val adventurePlatformModVersion: String by project
val clothConfigVersion: String by project
val adventureVersion: String by project
val adventurePlatformVersion: String by project
val cloudMojmapVersion: String by project
val neo_form_version: String by project

plugins {
    id("multiloader-common")
    id("net.neoforged.moddev")
}

neoForge {
    neoFormVersion = neo_form_version
    // Automatically enable AccessTransformers if the file exists
    val at = file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        accessTransformers.from(at.absolutePath)
    }
//    parchment {
//        minecraftVersion = parchment_minecraft
//        mappingsVersion = parchment_version
//    }
}

dependencies {
    // multiloader common
    compileOnly("org.spongepowered:mixin:0.8.5")
    compileOnly("io.github.llamalad7:mixinextras-common:0.5.3")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.3")
    compileOnly("org.ow2.asm:asm:9.9.1")

    //
    implementation(project(":base-common"))
    implementation("org.spongepowered:configurate-hocon:$configurateVersion")

    compileOnly("net.kyori:adventure-platform-mod-shared:$adventurePlatformModVersion")
    compileOnly("org.incendo:cloud-minecraft-modded-common:$cloudMojmapVersion")
    compileOnly("me.shedaniel.cloth:cloth-config:$clothConfigVersion")

    // TODO: adventure is being weird
    compileOnly("net.kyori:adventure-api:$adventureVersion")
    compileOnly("net.kyori:adventure-platform-api:$adventurePlatformVersion")
}

val commonJava by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

val commonResources by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

artifacts {
    add("commonJava", sourceSets["main"].java.srcDirs.single())
    add("commonResources", sourceSets["main"].resources.srcDirs.single())
}

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
listOf("apiElements", "runtimeElements", "sourcesElements", "javadocElements").forEach { variant ->
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