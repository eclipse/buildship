/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.test.fixtures

import groovy.transform.CompileStatic
import org.osgi.framework.Constants
import org.osgi.framework.ServiceRegistration

import org.eclipse.buildship.core.internal.CorePlugin

/**
 * Allows to replace services of the {@link CorePlugin} with arbitrary implementations for testing purposes.
 * <p/>
 * During the setup phase, the {@link #registerService(Class, Object)} is available to replace one or more
 * services. During the tear down phase, the {@link #close()} method needs to be called to restore the original implementation of all services.
 */
@CompileStatic
abstract class TestEnvironment implements Closeable {

    public static final TestEnvironment INSTANCE = new TestEnvironment(){}

    private static final int HIGHER_RANKING_THAN_PRODUCTION_CODE = 10

    private Map<String, ServiceRegistration> services = [:]

    TestEnvironment() {

    }

    def <T> void registerService(Class<T> definitionType, T implementation) {
        def serviceName = definitionType.name
        assert !services[serviceName] : "Service $serviceName already registred"
        def context = CorePlugin.instance.bundle.bundleContext
        def registration = context.registerService(serviceName, implementation, testingServicePreferences)
        services[serviceName] = registration
        implementation
    }

    private Dictionary<String, Object> getTestingServicePreferences() {
        def preferences = new Hashtable<String, Object>()
        preferences.put(Constants.SERVICE_RANKING, HIGHER_RANKING_THAN_PRODUCTION_CODE)
        return preferences
    }

    void close() {
        services.each { serviceName, service ->
            service.unregister()
        }
        services.clear()
    }
}
