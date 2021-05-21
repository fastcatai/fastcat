plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.9"
}

application {
    mainClass.set("app.Main")
    applicationDefaultJvmArgs = listOf(
            "-Djava.library.path=lib/opencv/x64",
            "-Dvlcj.library.path=lib/vlc",
            "-Dmediainfo.library.path=lib/mediainfo"
    )
    executableDir = "" // change location of start scripts to the root
}

javafx {
    version = rootProject.extra.get("javafxVersion") as String
    modules("javafx.controls", "javafx.fxml", "javafx.swing", "javafx.web")
}

dependencies {
    implementation(project(":commons"))
    implementation(project(":settings"))
    implementation(project(":image-annotation"))
    implementation(project(":video-review"))
    implementation(project(":video-annotation"))
    implementation(project(":io")) // TODO: Dependency needed for distribution, better way?
    implementation(project(":annotationV3")) // TODO: Dependency needed for distribution, better way?
    implementation(files("$rootDir/lib/opencv/opencv-4.3.0.jar"))
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.12.2")
    implementation(group = "org.slf4j", name = "slf4j-simple", version = "1.7.25")
    implementation(group = "org.apache.httpcomponents", name = "httpclient", version = "4.5.12")
    implementation(group = "org.apache.httpcomponents", name = "httpmime", version = "4.5.12")
    implementation(group = "org.kordamp.ikonli", name = "ikonli-javafx", version = "12.2.0")
    implementation(group = "org.kordamp.ikonli", name = "ikonli-materialdesign2-pack", version = "12.2.0")
}

// add library paths to property for run task
// (important when lib paths differ between run an distribution)
tasks.withType<JavaExec> {
    jvmArgs = listOf(
            "-Djava.library.path=$rootDir/lib/opencv/x64",
            "-Dvlcj.library.path=$rootDir/lib/vlc",
            "-Dmediainfo.library.path=$rootDir/lib/mediainfo"
    )
}

var jarArchiveFileName by extra("")

tasks.jar {
    // rename jar file
    archiveBaseName.set("${rootProject.name}-launcher")
    // save jar filename for start script
    jarArchiveFileName = archiveFileName.get()
}

tasks.startScripts {
    // change name of start scripts
    configurations.runtimeClasspath.get().allDependencies
    applicationName = "${rootProject.name}-$version"
    optsEnvironmentVar = "FAIV_OPTS"
    exitEnvironmentVar = "FAIV_EXIT_CONSOLE"
    windowsStartScriptGenerator = CustomWindowsStartScriptGenerator()
    doLast {
        // replace classpath for project built jar in start scripts
        var windowsScriptContent = windowsScript.readText().replace("%APP_HOME%\\lib\\$jarArchiveFileName",
                "%APP_HOME%\\modules\\$jarArchiveFileName")
        // get all dependent projects and replace classpath
        project.configurations.runtimeClasspath.get().allDependencies.withType<ProjectDependency>().forEach {
            val moduleJarName = it.dependencyProject.tasks.jar.get().archiveFileName.get()
            windowsScriptContent = windowsScriptContent.replace("%APP_HOME%\\lib\\$moduleJarName",
                    "%APP_HOME%\\modules\\$moduleJarName")
        }
        windowsScript.writeText(windowsScriptContent)
    }
}

// config distribution
distributions {
    main {
        contents {
            // change destination of project jar/bat
            eachFile {
                if (sourceName.matches(Regex("faiv.*\\.jar"))) {
                    relativePath.segments[relativePath.segments.size - 2] = "modules"
                }
            }
            // exclude unix start script until unix is supported
            exclude(tasks.startScripts.get().applicationName)
        }
    }
}