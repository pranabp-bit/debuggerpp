plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":vendor:slicing-core:core", "default"))

    implementation("guru.nidi:graphviz-java:0.18.1")
    implementation("de.tud.sse:soot-infoflow:2.9.0")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.json:json:20201115")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("org.jgrapht:jgrapht-core:1.2.0")
    implementation("commons-cli:commons-cli:1.4")
    implementation("org.slf4j:slf4j-simple:1.7.5")

    constraints {
        implementation("org.soot-oss:soot:4.3.0") {
            because("soot-infoflow:2.9.0 on maven central has a broken dependency")
        }
    }

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.2")
}