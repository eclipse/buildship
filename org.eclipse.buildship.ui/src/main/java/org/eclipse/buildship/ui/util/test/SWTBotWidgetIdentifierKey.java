/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.util.test;

/**
 * Contains the DEFAULT_KEY for clearly identifying widgets in the UI with the
 * SWTBot. This class uses the same key as the
 * org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences class and only exists
 * to avoid a dependency to the SWTBot plugin here.
 *
 */
public final class SWTBotWidgetIdentifierKey {

    private SWTBotWidgetIdentifierKey() {
    }

    /**
     * @see SWTBotPreferences#DEFAULT_KEY
     */
    private static final String KEY_DEFAULT_KEY = "org.eclipse.swtbot.search.defaultKey";

    /**
     * The default key used to match SWT widgets. Defaults to
     * {@code org.eclipse.swtbot.widget.key}. To set another default use the
     * system property
     * {@value org.eclipse.swtbot.swt.finder.utils.SWTBotPreferenceConstants#KEY_DEFAULT_KEY}
     * .
     */
    public static String DEFAULT_KEY = System.getProperty(KEY_DEFAULT_KEY, "org.eclipse.swtbot.widget.key");

}
