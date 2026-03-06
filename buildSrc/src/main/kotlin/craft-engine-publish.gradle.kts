import java.net.URI

plugins {
    `maven-publish`
}

val projectVersion = project.rootProject.property("project_version").toString()
val isSnapshot = projectVersion.endsWith("-SNAPSHOT")

publishing {
    repositories {
        maven {
            val repoName = if (isSnapshot) "snapshots" else "releases"
            name = repoName
            url = URI("https://repo.momirealms.net/$repoName")
            credentials {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
        }
    }

    publications {

    }
}