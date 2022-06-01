package Buildship

import java.util.EnumSet

enum class Jdk(val majorVersion: String, val vendor: String, val availableOn: EnumSet<OS> = EnumSet.allOf(OS::class.java)) {
    ORACLE_JDK_8("8", "oracle"),
    ORACLE_JDK_9("9", "oracle", EnumSet.of(OS.LINUX)),
    OPEN_JDK_11("11", "openjdk");

    fun getJavaHomePath(os: OS) = "%${os.name.toLowerCase()}.java${majorVersion}.${vendor}.64bit%"

    companion object {
        fun javaInstallationPathsProperty(os: OS) = "\"-Porg.gradle.java.installations.paths=${ Jdk.values().filter { it.availableOn.contains(os) }.joinToString(",") { it.getJavaHomePath(os) } }\""
    }
}