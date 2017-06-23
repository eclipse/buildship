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

package eclipsebuild.testing;

import java.io.File;

public class EclipseTestExtension {

    private String fragmentHost;

    /**
     * Application launched in Eclipse.
     * {@code org.eclipse.pde.junit.runtime.coretestapplication} can be used to run non-UI tests.
     */
    private String applicationName = "org.eclipse.pde.junit.runtime.uitestapplication";

    private File optionsFile;

    /** Boolean toggle to control whether to show Eclipse log or not. */
    private boolean consoleLog;

    private long testTimeoutSeconds = 60 * 60L;

    private String testEclipseJavaHome = System.getProperty("java.home");

    public String getApplicationName() {
        return this.applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public File getOptionsFile() {
        return this.optionsFile;
    }

    public void setOptionsFile(File optionsFile) {
        this.optionsFile = optionsFile;
    }

    public boolean isConsoleLog() {
        return this.consoleLog;
    }

    public void setConsoleLog(boolean consoleLog) {
        this.consoleLog = consoleLog;
    }

    public long getTestTimeoutSeconds() {
        return this.testTimeoutSeconds;
    }

    public void setTestTimeoutSeconds(long testTimeoutSeconds) {
        this.testTimeoutSeconds = testTimeoutSeconds;
    }

    public String getFragmentHost() {
        return this.fragmentHost;
    }

    public void setFragmentHost(String fragmentHost) {
        this.fragmentHost = fragmentHost;
    }

    public String getTestEclipseJavaHome() {
        return this.testEclipseJavaHome;
    }

    public void setTestEclipseJavaHome(String testEclipseJavaHome) {
        this.testEclipseJavaHome = testEclipseJavaHome;
    }

}
