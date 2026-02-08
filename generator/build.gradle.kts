plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:markdown:0.7.3")
    implementation("org.snakeyaml:snakeyaml-engine:2.8")
}

application {
    mainClass.set("blog.MainKt")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

tasks.register<JavaExec>("new") {
    group = "application"
    description = "Create a new article markdown file"
    mainClass.set("blog.ArticleCreator")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootProject.projectDir
}
