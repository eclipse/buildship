package eclipsebuild

abstract class UnZip extends UnPack {

    @Override
    void unpack(File zipFile, File targetDir) {
        def z = new java.util.zip.ZipFile(zipFile)
        targetDir.mkdirs()
        z.entries().findAll { !it.directory }.each {
            def outFile = new File("${targetDir.absolutePath}/${it}")
            outFile.parentFile.mkdirs()
            outFile.bytes = z.getInputStream(it).readAllBytes()
        }
    }
}
