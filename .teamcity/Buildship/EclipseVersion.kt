package Buildship

enum class EclipseVersion(val codeName: String, val versionNumber: String) {
    ECLIPSE4_8("Photon", "4.8"),
    ECLIPSE4_9("2018-09", "4.9"),
    ECLIPSE4_10("2018-12", "4.10"),
    ECLIPSE4_11("2019-03", "4.11"),
    ECLIPSE4_12("2019-06", "4.12"),
    ECLIPSE4_13("2019-09", "4.13"),
    ECLIPSE4_14("2019-12", "4.14"),
    ECLIPSE4_15("2020-03", "4.15"),
    ECLIPSE4_16("2020-06", "4.16"),
    ECLIPSE4_17("2020-09", "4.17"),
    ECLIPSE4_18("2020-12", "4.18"),
    ECLIPSE4_19("2021-03", "4.19"),
    ECLIPSE4_20("2021-06", "4.20"),
    ECLIPSE4_21("2021-09", "4.21"),
    ECLIPSE4_22("2021-12", "4.22"),
    ECLIPSE4_23("2022-03", "4.23"),
    ECLIPSE4_24("2022-06", "4.24"),
    ECLIPSE4_25("2022-09", "4.25"),
    ECLIPSE4_26("2022-12", "4.26"),
    ECLIPSE4_27("2023-03", "4.27");

    val updateSiteVersion: String
        get() = versionNumber.replace(".", "")

    val isLatest: Boolean
        get() = this == values().last()
}