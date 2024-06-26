import net.minecraftforge.gradle.userdev.UserDevExtension
import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency
import java.io.ByteArrayOutputStream

buildscript {
    repositories {
        maven { url = uri("https://maven.minecraftforge.net") }
        mavenCentral()
    }
    dependencies {
        classpath(group = "net.minecraftforge.gradle", name = "ForgeGradle", version = "6.0.+") {
            isChanging = true
        }
    }
}

val chestRefillId = findProperty("chestrefill.id") as String
val chestRefillName = findProperty("chestrefill.name") as String
val chestRefillVersion = findProperty("chestrefill.version") as String
val minecraftVersion = findProperty("minecraft.version") as String
val forgeVersion = findProperty("forge.version") as String
val spongeApiVersion = findProperty("sponge-api.version") as String

plugins {
    idea
    `java-library`
    `maven-publish`
    id("org.spongepowered.gradle.plugin") version "2.2.0"
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "io.github.aquerr"
version = "$chestRefillVersion-API-$spongeApiVersion"
apply(plugin = "net.minecraftforge.gradle")

description = "Plugin for restoring contents of a container after the specified time."

repositories {
    mavenCentral()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_21.majorVersion))
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

dependencies {
    "minecraft"("net.minecraftforge:forge:${forgeVersion}")
    api("org.spongepowered:spongeapi:${spongeApiVersion}")
    shadow("org.bstats:bstats-sponge:3.0.2")
}

tasks {
    jar {
        finalizedBy("shadowJar")

        if(System.getenv("JENKINS_HOME") != null) {
            project.version = project.version.toString() + "_" + System.getenv("BUILD_NUMBER")
            println("File name => " + archiveBaseName.get())
        } else {
            project.version = project.version.toString() + "-SNAPSHOT"
        }
    }

    shadowJar {
        finalizedBy("reobfJar")

        archiveClassifier.set("")

        relocate("org.bstats", "io.github.aquerr.chestrefill.lib.bstats")

        configurations = listOf(project.configurations.shadow.get())
    }
}

tasks.getByName("runServer").dependsOn(tasks.getByName("shadowJar"))

configure<UserDevExtension> {
    mappings("official", minecraftVersion)
}

sponge {
    apiVersion(spongeApiVersion)
    license("MIT")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin(chestRefillId) {
        displayName(chestRefillName)
        version(chestRefillVersion)
        entrypoint("io.github.aquerr.chestrefill.ChestRefill")
        description("Rebuilds destroyed blocks after specified time.")
        links {
            homepage("https://github.com/Aquerr/ChestRefill")
            source("https://github.com/Aquerr/ChestRefill")
            issues("https://github.com/Aquerr/ChestRefill/issues")
        }
        contributor("Aquerr") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

val getGitCommitDesc by tasks.registering(Exec::class) {
    commandLine("git", "log", "-1", "--pretty=%B")
    standardOutput = ByteArrayOutputStream()
    doLast {
        project.extra["gitCommitDesc"] = standardOutput.toString()
    }
}

tasks.register("printEnvironment") {
    doLast {
        System.getenv().forEach { key, value ->
            println("$key -> $value")
        }
    }
}

tasks.register("publishBuildOnDiscord") {
    dependsOn(getGitCommitDesc)
    group = "Publishing"
    description = "Task for publishing the jar file to discord's jenkins channel"
    doLast {
        val jarFiles: List<String> = groovy.ant.FileNameFinder().getFileNames(project.layout.buildDirectory.get().asFile.path, "**/*.jar")

        if(jarFiles.size > 0) {
            println("Found jar files: " + jarFiles)

            var lastCommitDescription = project.extra["gitCommitDesc"]
            if(lastCommitDescription == null || lastCommitDescription == "") {
                lastCommitDescription = "No changelog provided"
            }

            exec {
                commandLine("java", "-jar",  ".." + File.separator + "jenkinsdiscordbot-1.0.jar", "ChestRefill", jarFiles[0], lastCommitDescription)
            }
        }
    }
}

publishing {

    repositories {
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/Aquerr/ChestRefill")
            credentials {
                username = System.getenv("GITHUB_PUBLISHING_USERNAME")
                password = System.getenv("GITHUB_PUBLISHING_TOKEN")
            }
        }
    }

    publications {
        register<MavenPublication>(chestRefillId) {
            artifactId = chestRefillId
            description = project.description

            from(components["java"])
        }
    }
}