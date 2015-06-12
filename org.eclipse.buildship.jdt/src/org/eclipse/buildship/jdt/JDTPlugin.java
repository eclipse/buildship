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

package org.eclipse.buildship.jdt;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The plug-in runtime class for the Gradle integration plug-in containing the
 * JDT-related elements.
 * <p>
 * This class is automatically instantiated by the Eclipse runtime and wired
 * through the <tt>Bundle-Activator</tt> entry in the
 * <tt>META-INF/MANIFEST.MF</tt> file. The registered instance can be obtained
 * during runtime through the {@link JDTPlugin#getInstance()} method.
 */
public class JDTPlugin implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		JDTPlugin.context = bundleContext;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		JDTPlugin.context = null;
	}

}
