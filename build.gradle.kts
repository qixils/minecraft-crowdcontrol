plugins {
    id("java-library")
    id("io.freefair.lombok") version "6.4.1" apply false
    id("com.github.johnrengelman.shadow") version "7.1.2" apply true
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
            maven {
                name = "Minecraft Forge"
                url = uri("https://files.minecraftforge.net/maven/")
            }
        }
        mavenLocal() // TODO remove -- included to allow builds w/ 1.7.0-SNAPSHOT
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }

    if (project.name.endsWith("-platform")) {
        tasks {
            // TODO: disable output of non-shaded jars? or make their file names more obvious?
            // TODO: exclude kotlin directory in output jar? (i don't think it's used but gotta double check)

            build {
                dependsOn(shadowJar)
            }

            shadowJar {
                minimize() // minimize jar
                // exclude Java >8 META-INF files
                if (java.targetCompatibility.isJava8) {
                    exclude("META-INF/versions/")
                }
                // set name of output file to CrowdControl-XYZ-VERSION.jar
                val titleCaseName = project.name[0].toUpperCase() + project.name.substring(1, project.name.indexOf("-platform"))
                archiveBaseName.set("CrowdControl-$titleCaseName")
                archiveClassifier.set("")
            }
        }
    }

    // TODO: auto-copy default config into appropriate resource paths
    //   (i.e. /config.yml for Paper, /assets/crowd-control/default.conf for Sponge 7, etc.)
}
