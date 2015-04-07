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

package eclipsebuild.updatesite

/**
 * Utility class defining the layout of an update site.
 */
public class UpdateSiteLayout {

    static File getPluginsFolder(File updateSiteRoot) {
        return new File(updateSiteRoot, 'plugins')
    }

    static File getFeaturesFolder(File updateSiteRoot) {
        return new File(updateSiteRoot, 'features')
    }
}