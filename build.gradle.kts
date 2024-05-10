plugins {
    id("java-library") apply true
    id("io.freefair.lombok") version "8.6" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply true
    id("fabric-loom") version "1.6-SNAPSHOT" apply false
    id("xyz.jpenilla.run-paper") version "2.2.3" apply false // Adds runServer and runMojangMappedServer tasks for testing
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0" apply false // Generates plugin.yml
    id("org.spongepowered.gradle.plugin") version "2.2.0" apply false // Generates sponge_plugins.json and runServer task
    id("io.papermc.paperweight.userdev") version "1.7.0" apply false
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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
            maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
                name = "sonatype-oss-snapshots"
            }
            maven {
                name = "Jitpack"
                url = uri("https://jitpack.io")
            }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }

    tasks.shadowJar {
        relocate("org.reflections", "dev.qixils.relocated.reflections")
        relocate("com.google.guava", "dev.qixils.relocated.guava")

        if (project.name != "fabric-platform") {
            relocate("cloud.commandframework", "dev.qixils.relocated.cloud")
        }

    }



    if (project.name.endsWith("-platform")) {
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
}
