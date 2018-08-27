/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.ui.internal.console;

import java.net.URL;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.util.string.PatternUtils;

/**
 * Transforms all URLs to clickable links in the target console.
 * <p/>
 * When clicked, the URLs are opened in the external browser.
 *
 * @author Donat Csikos
 */
public final class UrlPatternMatchListener implements IPatternMatchListener {

    private TextConsole console;

    @Override
    public void connect(TextConsole console) {
        this.console = console;
    }

    @Override
    public void disconnect() {
        this.console = null;
    }

    @Override
    public void matchFound(PatternMatchEvent event) {
        try {
            int offset = event.getOffset();
            int length = event.getLength();
            final String url = this.console.getDocument().get(offset, length);
            this.console.addHyperlink(new Hyperlink(url), offset, length);
        } catch (BadLocationException e) {
        }
    }

    @Override
    public String getPattern() {
        return PatternUtils.WEB_URL_PATTERN;
    }

    @Override
    public int getCompilerFlags() {
        return 0;
    }

    @Override
    public String getLineQualifier() {
        return null;
    }

    /**
     * Simple {@link IHyperlink} implementation opening the target URL in the external browser.
     */
    static final class Hyperlink implements IHyperlink {

        private final String url;

        public Hyperlink(String url) {
            this.url = url;
        }

        @Override
        public void linkEntered() {
        }

        @Override
        public void linkExited() {
        }

        @Override
        public void linkActivated() {
            try {
                PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(this.url));
            } catch (Exception e) {
                CorePlugin.logger().warn("Cannot open " + this.url + " in external browser", e);
            }
        }
    }
}
