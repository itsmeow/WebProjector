pluginManagement {
  repositories {
    jcenter()
    maven(url = "http://maven.fabricmc.net/") {
      name = "Fabric"
    }
    maven(url = "https://kotlin.bintray.com/kotlinx") {
        name = "Kotlin X"
    }
    gradlePluginPortal()
    mavenCentral()
  }
}