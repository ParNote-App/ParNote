plugins {
    java
    kotlin("jvm") version "1.4.32"
    kotlin("kapt") version "1.4.32"
    id("io.vertx.vertx-plugin") version "1.2.0"
}

group = "com.parnote"
version = "1.0"

val vertxVersionVariable = "3.9.6"

repositories {
    jcenter()
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/iovertx-3720/")
    maven("https://jitpack.io")
}

vertx {
    mainVerticle = "com.parnote.Main"
    vertxVersion = vertxVersionVariable
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(group = "junit", name = "junit", version = "4.12")
    testImplementation("io.vertx:vertx-unit:$vertxVersionVariable")

    implementation("io.vertx:vertx-web:$vertxVersionVariable")
    implementation("io.vertx:vertx-mysql-postgresql-client:$vertxVersionVariable")
    implementation("io.vertx:vertx-sql-common:$vertxVersionVariable")
    implementation("io.vertx:vertx-mail-client:$vertxVersionVariable")
    implementation("io.vertx:vertx-lang-kotlin:$vertxVersionVariable")
    implementation("io.vertx:vertx-config:$vertxVersionVariable")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersionVariable")
    implementation("io.vertx:vertx-web-templ-handlebars:$vertxVersionVariable")

    implementation("com.beust:klaxon:5.4")
    implementation("org.slf4j:slf4j-simple:1.7.30")

    // dagger 2x
    implementation("com.google.dagger:dagger:2.34")
    kapt("com.google.dagger:dagger-compiler:2.34")

    // recaptcha v2 1.0.4
    implementation("com.github.triologygmbh:reCAPTCHA-V2-java:1.0.4")

    // https://mvnrepository.com/artifact/commons-codec/commons-codec
    implementation(group = "commons-codec", name = "commons-codec", version = "1.15")

    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.2")
    // Uncomment the next line if you want to use RSASSA-PSS (PS256, PS384, PS512) algorithms:
    //runtimeOnly("org.bouncycastle:bcprov-jdk15on:1.60")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.2")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    register("copyJar") {
        doLast {
            copy {
                from(shadowJar.get().archiveFile.get().asFile.absolutePath)
                into("./")
            }
        }

        dependsOn(shadowJar)
    }

    vertxDebug {
        environment("EnvironmentType", "DEVELOPMENT")
    }

    vertxRun {
        environment("EnvironmentType", "DEVELOPMENT")
    }

    build {
        dependsOn("copyJar")
    }

    register("buildDev") {
        dependsOn("build")
    }

    shadowJar {
        manifest {
            if (project.gradle.startParameter.taskNames.contains("buildDev"))
                attributes(mapOf("MODE" to "DEVELOPMENT"))
        }
    }

    jar {
        include("src/main/resources")
    }

    register("stage") {
    }
}