package org.eclipse.buildship.core.test.fixtures

import org.osgi.framework.BundleContext
import org.osgi.framework.Constants
import org.osgi.framework.ServiceRegistration

import com.google.common.base.Preconditions

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.Logger


/**
 * Provides facility to replace services with arbitrary implementations.
 * <p/>
 * On the setup phase the {@code testService()} method should be used to redefine a service. It has
 * to be cleaned up with the @{#cleanup()} method at the end of the execution.
 */
abstract class TestEnvironment {

    static def services = [:]

    static def testService(Class definitionType, def implementation) {
        Preconditions.checkState(services[definitionType.name] == null, "service ${definitionType.name} already initialised")
        def context = CorePlugin.instance.bundle.bundleContext
        def registration = registerService(context, definitionType, implementation, testingServicePreferences);
        services[definitionType.name] = registration
        implementation
    }

    static def cleanup() {
        services.each { serviceName, service ->
            service.unregister()
        }
        services.clear()
    }

    private static def getTestingServicePreferences() {
        def preferences = new Hashtable<String, Object>();
        // ranking has to be higher than all services registered in the plugins
        preferences.put(Constants.SERVICE_RANKING, 10);
        preferences
    }

    private static def <T> ServiceRegistration registerService(BundleContext context, Class<T> clazz, T service, Dictionary<String, Object> properties) {
        context.registerService(clazz.getName(), service, properties);
    }
}
