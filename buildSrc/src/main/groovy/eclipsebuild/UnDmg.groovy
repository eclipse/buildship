package eclipsebuild

import org.apache.commons.io.FileUtils

abstract class UnDmg extends UnPack {
    @Override
    void unpack(File tarFile, File targetDir) {
        try {
            Runtime.getRuntime().exec("hdiutil attach $tarFile.absolutePath").waitFor()
            FileUtils.copyDirectory(new File('/Volumes/Eclipse/Eclipse.app'), new File(targetDir, 'Eclipse.app'))
        } finally {
            Runtime.getRuntime().exec("hdiutil detach /Volumes/Eclipse").waitFor()
        }
    }
}