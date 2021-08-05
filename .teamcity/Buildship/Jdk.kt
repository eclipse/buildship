package Buildship

enum class Jdk(val majorVersion: String, val vendor: String) {
    ORACLE_JDK_8("8", "oracle"),
    ORACLE_JDK_9("9", "oracle"),
    OPEN_JDK_11("11", "openjdk");

    fun getJavaHomePath(os: OS) = "%${os.name.toLowerCase()}.java${majorVersion}.${vendor}.64bit%"
    fun getJavaCompilerPath(os: OS) = """%${os.name.toLowerCase()}.java${majorVersion}.${vendor}.64bit%${os.pathSeparator}bin${os.pathSeparator}javac"""
}