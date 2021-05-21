plugins {
    distribution
    idea
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

val javafxVersion by extra("13.0.2")

allprojects {

    repositories {
        mavenCentral()
        maven {
            url = uri("https://jitpack.io") // mainly for 'PDFViewerFX'
        }
    }

    group = "edu.uniwue"
    version = rootDir.resolve("commons/src/main/resources/version").readText().trim()
}

subprojects {
    apply(plugin = "java")

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()
    }

    tasks.withType<Jar> {
        // add manifest attributes to all subprojects
        manifest {
            attributes(
                    mapOf(
                            "Created-By" to "Kevin Makowski"
                    )
            )
        }
    }
}

tasks.distTar {
    enabled = false // disable the creation of tar files
}

// get all libraries from launcher an merge into a single zip
tasks.distZip {
    from(project(":launcher").tasks.distZip.get().inputs.files) {
        into("${project.name}-${project.version}/lib")
        // change destination of project jar/bat
        eachFile {
            println(sourceName)
            if (sourceName.matches(Regex("faiv.*\\.jar"))) {
                relativePath.segments[relativePath.segments.size - 2] = "modules"
            } else if (sourceName.matches(Regex("faiv.*\\.bat"))) {
                val pathSegments = relativePath.segments.toMutableList()
                pathSegments.removeAt(relativePath.segments.lastIndex - 1)
                relativePath = RelativePath(true, *pathSegments.toTypedArray())
            }
        }
    }
}

tasks.installDist {
    from(project(":launcher").tasks.installDist) {
        into("")
    }
}

// copy all native libraries into the build folder
tasks.register<Copy>("copyNativeLibs") {
    from(fileTree("$projectDir/lib")) {
        exclude("**/opencv/*.jar")
    }
    into("$buildDir/natives")
}

// config distribution
distributions {
    main {
        contents {
            // copy native libs into folder lib
            from(tasks.named("copyNativeLibs")) {
                into("lib")
            }
        }
    }
}
