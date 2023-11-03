package Buildship

enum class ScenarioType(val gradleTasks: String) {
    SANITY_CHECK("assemble checkstyleMain"),
    BASIC_COVERAGE("clean eclipseTest"),
    FULL_COVERAGE("clean build"),
    CROSS_VERSION("clean crossVersionEclipseTest")
}
