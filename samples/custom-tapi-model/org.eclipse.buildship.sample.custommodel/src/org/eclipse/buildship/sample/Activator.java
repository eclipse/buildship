package org.eclipse.buildship.sample;

import java.io.File;

import org.osgi.framework.BundleContext;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Plugin;

public class Activator extends Plugin {

    private static File bundleLocation;
    private static Plugin plugin;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        plugin = this;
        bundleLocation = new File(FileLocator.resolve(bundleContext.getBundle().getEntry("/")).toURI());
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        plugin = null;
    }

    public static Plugin getInstance() {
        return plugin;
    }

    public static File getBundleLocation() {
        return bundleLocation;
    }
}
