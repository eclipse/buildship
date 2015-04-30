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

/**
 * Holds the source and destination folders for a signing script.
 *
 */
final class SigningFolders {

    File unsignedFolder
    File signedFolder

    SigningFolders(File unsignedFolder, File signedFolder) {
        this.unsignedFolder = unsignedFolder
        this.signedFolder = signedFolder
    }
}
