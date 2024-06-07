val cloudVersion: String by project

description = "Minecraft Crowd Control: Sponge 11"

val targetJavaVersion = 21
tasks.withType<JavaCompile>().configureEach {
    options.release.set(targetJavaVersion)
}

sourceSets {
    main {
        java {
            srcDir(project(":sponge8-platform").sourceSets["main"].java.srcDirs)
        }
    }
}

tasks {
    compileJava {
        // Configure compileJava to exclude conflicting classes from `bar`
        options.compilerArgs.add("-Xlint:-path")

        // Collect the list of classes from `foo` dynamically
        doFirst {
            val fooClasses = source.files.flatMap { file ->
                if (file.isDirectory) {
                    file.walkTopDown().filter { it.isFile && it.extension == "java" }
                } else if (file.isFile && file.extension == "java") {
                    sequenceOf(file)
                } else {
                    emptySequence()
                }
            }.map { it.relativeTo(projectDir).path }.toList()

            source = source.filter { file ->
                // Define the path of the class files to be excluded from `bar`
                val relativePath = file.relativeTo(project(":sponge8-platform").projectDir).path
                !fooClasses.contains(relativePath)
            }.asFileTree
        }
    }
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

plugins {
    id("org.spongepowered.gradle.plugin")
}

repositories {
    maven {
        name = "Sponge"
        url = uri("https://repo.spongepowered.org/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":configurate-common")) {
        exclude(group = "com.google.code.gson")
        exclude(group = "com.google.auto.service")
        exclude(group = "com.google.inject")
        exclude(group = "com.google.guava")
        exclude(group = "com.google.errorprone")
        exclude(group = "com.google.j2objc")
        exclude(group = "com.google.code.findbugs")
    }
    implementation("com.github.qixils.cloud:cloud-sponge:$cloudVersion")
    compileOnly("org.spongepowered:spongeapi:11.0.0-SNAPSHOT") {
        exclude(group = "net.kyori", module = "adventure-api")
    }
    //compileOnly("org.spongepowered:sponge:1.20.6-11.0.0-SNAPSHOT:dev")
}

sponge {
    apiVersion("11.0.0-SNAPSHOT")
    loader {
        name(org.spongepowered.gradle.plugin.config.PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    license("Mozilla Public License Version 2.0")
    plugin("crowdcontrol") {
        displayName("Crowd Control")
        version(project.version.toString() + "+sponge11")
        entrypoint("dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin")
        description("The Ultimate Interactive Experience for Streamers")
        links {
            homepage("https://github.com/qixils/minecraft-crowdcontrol")
            source("https://github.com/qixils/minecraft-crowdcontrol")
            issues("https://github.com/qixils/minecraft-crowdcontrol/issues")
        }
        contributor("qixils") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(org.spongepowered.plugin.metadata.model.PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

tasks {
    shadowJar {
        exclude("org/slf4j/")
        exclude("io/leangen/geantyref/")

        dependencies {
            exclude("org.slf4j::")
            exclude("io.leangen.geantyref::")
        }
    }
}
