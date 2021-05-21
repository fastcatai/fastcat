plugins {
    id("org.openjfx.javafxplugin") version "0.0.9"
}

javafx {
    version = rootProject.extra.get("javafxVersion") as String
    modules("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation(files("$rootDir/lib/opencv/opencv-4.3.0.jar"))
    implementation(group = "uk.co.caprica", name = "vlcj", version = "4.4.0")
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.12.2")
    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.11")
    compileOnly(group = "org.apache.logging.log4j", name = "log4j-api", version = "2.14.1")
    compileOnly(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.14.1")
}

tasks.jar {
    // rename jar file
    archiveBaseName.set("${rootProject.name}-commons")
}