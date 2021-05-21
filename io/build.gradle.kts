plugins {
    id("org.openjfx.javafxplugin") version "0.0.9"
}

javafx {
    version = rootProject.extra.get("javafxVersion") as String
    modules("javafx.base", "javafx.graphics")
}

dependencies {
    implementation(project(":commons"))
    implementation(project(":annotationV3"))
    implementation(project(":settings"))
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.12.2")
    compileOnly(group = "org.apache.logging.log4j", name = "log4j-api", version = "2.14.1")
    compileOnly(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.14.1")
}

tasks.jar {
    // rename jar file
    archiveBaseName.set("${rootProject.name}-io")
}