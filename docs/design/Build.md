# Gradle-based Build

## Building

Buildship is built using a set of plugins that support dependency resolution, processing Eclipse descriptors, and
publishing Eclipse update sites. The scripts for the plugins are located in the _buildSrc_ folder.

The plugin scripts in _buildSrc_ expect a small set of Eclipse bundles/libraries in the
_buildSrc/libs_ folder that are committed to the Git repository to avoid bootstrapping problems. All
plugins are defined in the `eclipsebuild` package.

The plugin version is specified in the _version.txt_ file.

### BuildDefinitionPlugin

The `BuildDefinitionPlugin` Gradle plugin has to be applied on the root project. It defines the target platform for the build. 
The BundlePlugin, FeaturePlugin and UpdateSitePlugin depend on the tasks defined here which make all Eclipse plugins
available for dependency resolution.

The build definition can specify a set of target platforms in a DSL. For example

    apply plugin: eclipsebuild.BuildDefinitionPlugin

    eclipseBuild {
        defaultEclipseVersion = '44'
     
        targetPlatform {
            eclipseVersion = '44'
            sdkVersion = "4.4.2.M20150204-1700"
            updateSites = [
                "http://download.eclipse.org/release/luna",
                "http://download.eclipse.org/technology/swtbot/releases/latest/"
            ]
            features = [
               "org.eclipse.swtbot.eclipse.feature.group",
               "org.eclipse.swtbot.ide.feature.group"
            ]
        }
     
        targetPlatform {
             eclipseVersion = '43'
             ...
         }
     }

If a `gradle build` is executed, it will automatically use the Eclipse version `44` for the target platform. An arbitrary
Target platform can be selected with by specifying the `eclipse.version` property:
    
    gradle build -Peclipse.version=43
    
`installTargetPlatform` is the task creating the target platform. It does the following:

1. Assembles an Eclipse SDK with the specified version from the update sites,
2. installs the specified features from the update sites into the SDK and
3. converts all plugins from this SDK into a Maven repository

With the 'mavenized' target platform available the builds can reference plugin dependencies like normal. 
The groupId is always `eclipse`.

    compile "eclipse:org.eclipse.jdt.ui:+"

The location of the generated target platform is _~/.tooling/eclipse/targetPlatforms_. It can be redefined by specifying 
the `targetPlatformsDir` property:

    gradle installTargetPlatform -PtargetPlatformsDir=/path/to/target/platform


### BundlePlugin

Eclipse plugins can be build with the `BundlePlugin` It applies the Java plugin and configures the build based on
the descriptors.The configuration is based on the content of the _build.properties_ file. Once built, the bundle
manifest file _META-INF/MANIFEST.MF_ is copied and the version is replaced with the one from the current build.

The `BundlePlugin` also adds the capability to bundle jars along with the dependencies from the target platform.
This can be used by declaring a `bundled` dependency.

    apply plugin: eclipsebuild.BundlePlugin

    dependencies {
        compile "eclipse:org.eclipse.core.runtime:+"
        bundled "com.google.guava:guava:+"
    }

The dependencies of the `bundled` configuration are used by the `updateLibs` task (including transitives).
This task downloads the jars into the _lib_ folder, updates the manifest file to reference the jars and updates .classpath file.

### TestBundlePlugin

The `TestBundlePlugin` Gradle plugin is an extension of the `BundlePlugin` and knows how to run tests.

    apply plugin: eclipsebuild.TestBundlePlugin

    eclipseTest {
        fragmentHost 'org.eclipse.buildship.core'
        applicationName 'org.eclipse.pde.junit.runtime.coretestapplication'
        optionsFile file('.options')
    }

The plugin adds a task called `eclipseTest`. This task does the following:

1. Clears the _buildDir/eclipseTest_ folder in order to have a clean environment for testing.
2. Copies the non-mavenized target platform to the _buildDir/eclipseTest_ folder.
3. Installs the project dependency plugins into the target platform with the _P2 director_.
4. Executes the target platform and runs the tests with a custom Gradle test runner (which is an `ITestRunListener` implementation).

Currently all concrete classes are gathered from the test plugin for execion. The test results are collected in the build/reports folder.

The implementation uses techniques defined in article [PDE Unit Tests using Ant](http://www.eclipse.org/articles/article.php?file=Article-PDEJUnitAntAutomation/index.html).

### FeaturePlugin

The `FeaturePlugin` Gradle plugin knows how to build an Eclipse feature. When built it creates a jar containing all elements 
defined in the project's _build.properties_.

### UpdateSitePlugin

The `UpdateSitePlugin` Gradle plugin knows how to build an update site. It adds a task called `createP2Repository`. This task
takes all project dependencies and, if they are either Eclipse plugins or features, publishes them to a local P2 repository. The
generated repository is available in the _build/repository_ folder. An example DSL for an update site:

    apply plugin: eclipsebuild.UpdateSitePlugin

    updateSite {
        siteDescriptor = file("category.xml")
        signBundles = true
    }

The `siteDescriptor` should point to a category descriptor which is used to publish update sites. The `signBundles` specifies
whether the update site should be signed (default is false).

### Adding a new Eclipse plugin

* Create a new folder under the _buildship_ root folder
* Create a project in that folder through Eclipse and use the same name for the project as for the folder
* Create a _build.gradle_ file and apply the `BundlePlugin`
* Add the project to the _settings.gradle_ file
