plugins {
    id("org.openjfx.javafxplugin") version "0.0.9"
}

javafx {
    version = rootProject.extra.get("javafxVersion") as String
    modules("javafx.controls" ,"javafx.fxml")
}

dependencies {
    implementation(project(":commons"))
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.12.2")
    implementation(group = "com.fasterxml.jackson.dataformat", name = "jackson-dataformat-yaml", version = "2.10.2")
    implementation(group = "org.kordamp.ikonli", name = "ikonli-javafx", version = "12.2.0")
    implementation(group = "org.kordamp.ikonli", name = "ikonli-materialdesign2-pack", version = "12.2.0")
    implementation(group = "org.apache.logging.log4j", name = "log4j-api", version = "2.14.1")
    implementation(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.14.1")
}

tasks.jar {
    // rename jar file
    archiveBaseName.set("${rootProject.name}-settings")
}