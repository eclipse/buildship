/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package eclipsebuild

import java.nio.channels.FileLock

/**
 * Utility class for inter-process-synchronization by using a {@code FileLock} as a semaphore.
 * <p/>
 * When the {@link FileSemaphore#lock()} method is called, a .lock file is created under a given folder guarded by
 * the FileChannel.lock(). This lock can be released again by calling the {@link FileSemaphore#unlock()} method.
 */
class FileSemaphore {

    final File directory
    FileOutputStream fos
    FileLock lock

    FileSemaphore(File directory) {
        this.directory = directory
    }

    def lock() {
        File lockFile = new File(directory, ".lock")
        if (!lockFile.exists()) {
            lockFile.getParentFile().mkdirs()
            lockFile.createNewFile()
        }
        fos = new FileOutputStream(lockFile)
        lock = fos.getChannel().lock()
    }

    def unlock() {
        lock.release()
        fos.close()
    }

}
