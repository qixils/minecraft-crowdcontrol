val nettyVersion: String by project
val mojmapVersion: String by project

plugins {
    id("java-library") apply true
    id("io.freefair.lombok") version "8.14" apply false
    id("com.gradleup.shadow") version "8.3.7" apply true
    id("dev.architectury.loom") version "1.11-SNAPSHOT" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1" apply false // Adds runServer and runMojangMappedServer tasks for testing
    id("de.eldoria.plugin-yml.bukkit") version "0.8.0" apply false // Generates plugin.yml
    //id("io.papermc.paperweight.userdev") version "1.7.7" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply true
    id("me.modmuss50.mod-publish-plugin") version "1.1.0" apply false
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

architectury {
    minecraft = mojmapVersion
}

subprojects {
    apply {
        plugin("java-library")
        plugin("io.freefair.lombok")
        plugin("com.gradleup.shadow")
    }

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "PaperMC"
        }
        maven("https://files.minecraftforge.net/maven/") {
            name = "Minecraft Forge"
        }
        maven("https://maven.neoforged.net/releases") {
            name = "NeoForged"
        }
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
            name = "sonatype-oss-snapshots"
        }
        maven("https://jitpack.io") {
            name = "Jitpack"
        }
        mavenCentral()
        mavenLocal()
    }

    dependencies {
        compileOnly("io.netty:netty-buffer:$nettyVersion")
    }

    val targetJavaVersion = 21
    tasks.withType<JavaCompile>().configureEach {
        options.release.set(targetJavaVersion)
        options.encoding = Charsets.UTF_8.name()
    }

    java {
        val javaVersion = JavaVersion.toVersion(targetJavaVersion)
        if (JavaVersion.current() < javaVersion) {
            toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
        }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
//    withSourcesJar()
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
