val nettyVersion: String by project
val mojmapVersion: String by project

plugins {
    id("java-library") apply true
    id("io.freefair.lombok") version "8.6" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply true
    id("dev.architectury.loom") version "1.7-SNAPSHOT" apply false
    id("xyz.jpenilla.run-paper") version "2.3.0" apply false // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" apply false // Generates plugin.yml
    id("org.spongepowered.gradle.plugin") version "2.2.0" apply false // Generates sponge_plugins.json and runServer task
    id("io.papermc.paperweight.userdev") version "1.7.1" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply true
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

architectury {
    minecraft = mojmapVersion
}

subprojects {
    apply {
        plugin("java-library")
        plugin("io.freefair.lombok")
        plugin("com.github.johnrengelman.shadow")
    }

    repositories {
        mavenCentral()
        repositories {
            maven(url = "https://files.minecraftforge.net/maven/") {
                name = "Minecraft Forge"
            }
            maven(url = "https://maven.neoforged.net/releases") {
                name = "NeoForged"
            }
            maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
                name = "sonatype-oss-snapshots"
            }
            maven(url = "https://jitpack.io") {
                name = "Jitpack"
            }
        }
    }

    dependencies {
        compileOnly("io.netty:netty-buffer:$nettyVersion")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }

    val isPlatform = project.name.endsWith("-platform")
    val isModded = listOf("mojmap-common", "fabric-platform", "neoforge-platform").contains(project.name)

    tasks.shadowJar {
        relocate("net.kyori.adventure.text.minimessage", "dev.qixils.relocated.adventure.minimessage")
        relocate("net.kyori.adventure.text.serializer.legacy", "dev.qixils.relocated.adventure.serializer.legacy")
        relocate("net.kyori.adventure.text.serializer.plain", "dev.qixils.relocated.adventure.serializer.plain")
        relocate("net.kyori.adventure.serializer", "dev.qixils.relocated.adventure.serializer")
        relocate("org.jetbrains.annotations", "dev.qixils.relocated.annotations")
        relocate("org.intellij.lang.annotations", "dev.qixils.relocated.annotations.alt")
        relocate("javassist", "dev.qixils.relocated.javassist")
        relocate("javax.annotation", "dev.qixils.relocated.javax.annotation")
        relocate("org.checkerframework", "dev.qixils.relocated.checkerframework")

        if (!isModded) {
            relocate("org.incendo.cloud", "dev.qixils.relocated.cloud")
        }
    }

    if (isPlatform) {
        // inherit resources from common module
        sourceSets.main { resources.srcDir(project(":base-common").sourceSets["main"].resources.srcDirs) }

        tasks {
            // TODO: disable output of non-shaded jars? or make their file names more obvious?
            shadowJar {
                // exclude Java >8 META-INF files
                if (java.targetCompatibility.isJava8) {
                    exclude("META-INF/versions/")
                }
                // set name of output file to CrowdControl-XYZ-VERSION.jar
                val titleCaseName = project.name[0].uppercaseChar() + project.name.substring(1, project.name.indexOf("-platform"))
                archiveBaseName.set("CrowdControl-$titleCaseName")
                archiveClassifier.set("")
            }
        }

        if (project.name != "fabric-platform") {
            tasks {
                build {
                    dependsOn(shadowJar)
                }
            }
        }
    }

    if (isModded) {
        repositories {
            maven(url = "https://oss.sonatype.org/content/repositories/snapshots") {
                name = "Sonatype Snapshots"
            }
            maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots") {
                name = "Sonatype Snapshots 2"
            }
            maven(url = "https://maven.shedaniel.me") {
                name = "Shedaniel"
            }

            exclusiveContent {
                forRepository {
                    maven(url = "https://api.modrinth.com/maven") {
                        name = "Modrinth"
                    }
                }
                filter {
                    includeGroup("maven.modrinth")
                }
            }
        }
    }
}
