package Buildship

enum class EclipseVersion(val codeName: String, val versionNumber: String) {
    ECLIPSE4_3("Kepler", "4.3"),
    ECLIPSE4_4("Luna", "4.4"),
    ECLIPSE4_5("Mars", "4.5"),
    ECLIPSE4_6("Neon", "4.6"),
    ECLIPSE4_7("Oxygen", "4.7"),
    ECLIPSE4_8("Photon", "4.8"),
    ECLIPSE4_9("2018-09", "4.9"),
    ECLIPSE4_10("2018-12", "4.10"),
    ECLIPSE4_11("2019-03", "4.11"),
    ECLIPSE4_12("2019-06", "4.12"),
    ECLIPSE4_13("2019-09", "4.13"),
    ECLIPSE4_14("2019-12", "4.14"),
    ECLIPSE4_15("2020-03", "4.15"),
    ECLIPSE4_16("2020-06", "4.16");

    val updateSiteVersion: String
        get() = versionNumber.replace(".", "")
}