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

import com.google.common.base.Preconditions

import java.nio.channels.FileLock

/**
 * Utility class to lock and unlock based on a location of a folder.
 * <p/>
 * When the {@link #lock()} method is called a .lock file is created under the folder guarded by
 * the FileChannel.lock(). This lock can be released by calling the {@link #unlock()} method.
 */
class DirectoryLock {

    final File directory
    FileOutputStream fos
    FileLock lock

    DirectoryLock(File directory) {
        if (directory == null) {
            throw new IllegalArgumentException("Argument can't be null")
        }
        this.directory = directory
    }

    def lock() {
        File dotLock = new File(directory, ".lock")
        if (!dotLock.exists()) {
            dotLock.getParentFile().mkdirs()
            dotLock.createNewFile()
        }
        fos = new FileOutputStream(dotLock)
        lock = fos.getChannel().lock()
    }

    def unlock() {
        lock.release()
        fos.close()
    }
}
