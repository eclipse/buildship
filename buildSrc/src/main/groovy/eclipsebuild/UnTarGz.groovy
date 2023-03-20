package eclipsebuild

import org.apache.commons.io.IOUtils
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.util.zip.GZIPInputStream

abstract class UnTarGz extends UnPack {
    @Override
    void unpack(File tarFile, File targetDir) {
        def tin = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(tarFile)));
        targetDir.mkdirs()
        def entry = tin.nextTarEntry;
        while (entry != null) {
            if (!entry.isDirectory()) {

                File curfile = new File(targetDir, entry.getName());
                File parent = curfile.getParentFile();
                parent.mkdirs()
                try (def fos = new FileOutputStream(curfile)) {
                    IOUtils.copy(tin, fos)
                }
            }
            entry = tin.nextTarEntry;
        }
    }
}
