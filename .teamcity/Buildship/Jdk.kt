package Buildship

enum class Jdk(val majorVersion: String) {
    ORACLE_JDK_8("8"),
    ORACLE_JDK_9("9");

    fun getJavaHomePath(os: OS) = "%${os.name.toLowerCase()}.java${majorVersion}.oracle.64bit%"
    fun getJavaCompilerPath(os: OS) = """%${os.name.toLowerCase()}.java${majorVersion}.oracle.64bit%${os.pathSeparator}bin${os.pathSeparator}javac"""
}