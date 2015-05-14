package org.eclipse.buildship.core.test.fixtures

import com.google.common.base.Preconditions
import org.eclipse.buildship.core.CorePlugin
import org.osgi.framework.BundleContext
import org.osgi.framework.Constants
import org.osgi.framework.ServiceRegistration

/**
 * Provides facility to replace services of the {@link CorePlugin} with arbitrary implementations for testing purposes.
 * <p/>
 * During the setup phase, the {@link TestEnvironment#registerService(Class, Object)} is available to replace one or more
 * services. During the tear down phase, the service registry has to be cleaned up with {@link TestEnvironment#cleanup()}.
 */
abstract class TestEnvironment {

    private static def services = [:]

    static <T> void registerService(Class<T> definitionType, T implementation) {
        Preconditions.checkState(services[definitionType.name] == null, "service ${definitionType.name} already initialised" as Object)
        def context = CorePlugin.instance.bundle.bundleContext
        def registration = internalRegisterService(context, definitionType, implementation, testingServicePreferences);
        services[definitionType.name] = registration
        implementation
    }

    private static def getTestingServicePreferences() {
        // ranking has to be higher than all services registered in the plugins
        def preferences = new Hashtable<String, Object>();
        preferences.put(Constants.SERVICE_RANKING, 10);
        preferences
    }

    private static def <T> ServiceRegistration internalRegisterService(BundleContext context, Class<T> clazz, T service, Dictionary<String, Object> properties) {
        context.registerService(clazz.getName(), service, properties);
    }

    static def cleanup() {
        services.each { serviceName, service ->
            service.unregister()
        }
        services.clear()
    }

}
