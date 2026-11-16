import me.modmuss50.mpp.PublishModTask

val nettyVersion = project.property("nettyVersion") as String
val minecraft_version = project.property("minecraft_version") as String

plugins {
    id("java-library") apply true
    id("io.freefair.lombok") version "9.8.1" apply false
    id("com.gradleup.shadow") version "9.6.1" apply true
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT" apply false
    id("net.neoforged.moddev") version "2.0.147" apply false
    id("xyz.jpenilla.run-paper") version "3.1.0" apply false // Adds runServer and runMojangMappedServer tasks for testing
    id("de.eldoria.plugin-yml.bukkit") version "0.9.0" apply false // Generates plugin.yml
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.24" apply false
    id("me.modmuss50.mod-publish-plugin") version "2.2.1" apply false
    id("io.papermc.hangar-publish-plugin") version "0.1.4" apply false
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

subprojects {
    apply {
        plugin("java-library")
        plugin("io.freefair.lombok")
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") {
            name = "PaperMC"
        }
        maven("https://files.minecraftforge.net/maven/") {
            name = "Minecraft Forge"
        }
        exclusiveContent {
            forRepository {
                maven("https://maven.neoforged.net/releases") {
                    name = "NeoForged"
                }
            }
            filter {
                // other repositories (i.e. PaperMC) have the metadata but not the jars
                includeGroup("cpw.mods")
            }
        }
        maven("https://libraries.minecraft.net") {
            name = "Minecraft Libraries"
        }
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
            name = "sonatype-oss-snapshots"
        }
        maven("https://maven.isxander.dev/releases") {
            name = "Xander Maven"
        }
        maven("https://jitpack.io") {
            name = "Jitpack"
        }
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

    if (isPlatform) {
        // inherit resources from common module
        sourceSets.main { resources.srcDir(project(":base-common").sourceSets["main"].resources.srcDirs) }

        // paper still uses shadowjar
        if (isModded) {
            tasks {
                jar {
                    // set name of output file to CrowdControl-PLATFORM+VERSION.jar
                    var titleCaseName = project.name[0].uppercaseChar() + project.name.substring(1, project.name.indexOf("-platform"))
                    if (titleCaseName == "Neoforge") titleCaseName = "NeoForge"
                    archiveBaseName.set("CrowdControl-$titleCaseName+$minecraft_version-$version")
                    archiveClassifier.set("")
                    archiveVersion.set("")
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

tasks.register("release") {
    group = "publishing"
    description = "Builds every module, then publishes the platform jars"
    dependsOn(subprojects.map { "${it.path}:build" })
    dependsOn(subprojects.filter { it.name.endsWith("-platform") }.map { "${it.path}:publishMods" })
    dependsOn("paper-platform:publishAllPublicationsToHangar")
}

subprojects {
    plugins.withId("me.modmuss50.mod-publish-plugin") {
        tasks.withType<PublishModTask>().configureEach {
            // when releasing, wait for everything to build so that a failure stops all platforms alike
            mustRunAfter(rootProject.subprojects.map { "${it.path}:build" })
        }
    }
    // hangar equivalent is in its build file
}
