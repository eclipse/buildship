# Public API Examples

## `org.eclipse.buildship.core.invocationcustomizers` extension point

### plugin.xml

    <extension point="org.eclipse.buildship.core.invocationcustomizers">
        <customizer class="EclipseInstallLocationGradleBuild" />
    </extension>



### EclipseInstallLocationGradleBuild.java

    import java.util.*;
    import org.eclipse.core.runtime.Platform;
    import org.eclipse.buildship.core.invocation.InvocationCustomizer;
    
    public class EclipseInstallLocationGradleBuild implements InvocationCustomizer {
         @Override public List<String> getExtraArguments() {
            return Arrays.asList("-PeclipseInstallLocation=" + Platform.getLocation().toPortableString());
        }
    }


### build.gradle

    task printEclipseLocation {
        doLast {
            if (project.hasProperty("eclipseInstallLocation")) {
                println project.getProperty("eclipseInstallLocation")
            }
        }
    }

### execution output

    :printEclipseLocation
    /path/to/install/location
    
    BUILD SUCCESSFUL
    
    Total time: 0.056 secs
