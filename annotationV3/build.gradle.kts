plugins {
    id("org.openjfx.javafxplugin") version "0.0.9"
}

javafx {
    version = rootProject.extra.get("javafxVersion") as String
    modules("javafx.controls")
}

dependencies {
    implementation(project(":commons"))
    implementation(project(":settings"))
    compileOnly(group = "org.apache.logging.log4j", name = "log4j-api", version = "2.14.1")
    compileOnly(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.14.1")
}

tasks.jar {
    // rename jar file
    archiveBaseName.set("${rootProject.name}-annotation")
}
