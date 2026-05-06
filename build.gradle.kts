val nettyVersion: String by project
val minecraft_version: String by project

plugins {
    id("java-library") apply true
    id("io.freefair.lombok") version "9.2.0" apply false
    id("com.gradleup.shadow") version "8.3.10" apply true
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT" apply false
    id("net.neoforged.moddev") version "2.0.141" apply false
    id("xyz.jpenilla.run-paper") version "3.0.2" apply false // Adds runServer and runMojangMappedServer tasks for testing
    id("de.eldoria.plugin-yml.bukkit") version "0.9.0" apply false // Generates plugin.yml
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
    id("me.modmuss50.mod-publish-plugin") version "1.1.0" apply false
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
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
        maven("https://maven.neoforged.net/releases") {
            name = "NeoForged"
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

    val targetJavaVersion = 25
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
