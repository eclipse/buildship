# Gradle-based Build

## Building

Buildship is built using a set of plugins that support dependency resolution, processing Eclipse descriptors, and
publishing Eclipse update sites. The scripts for the plugins are located in the _buildSrc_ folder.

The plugin scripts in _buildSrc_ expect a small set of Eclipse bundles/libraries in the
_buildSrc/libs_ folder that are committed to the Git repository to avoid bootstrapping problems. All
plugins are defined in the `eclipsebuild` package.

The plugin version is specified in the _version.txt_ file.

### BuildDefinitionPlugin

The `BuildDefinitionPlugin` Gradle plugin has to be applied on the root project. It defines the target platform for the build. It
contributes the `downloadEclipseSdk` and the `installTargetPlatform` tasks which are hooked into every build. This
ensures that the target platform plugins are available for dependency resolution. The location of the target
platform is _~/.tooling/eclipse/targetPlatforms_.

The build definition specifies the default target Eclipse version which we build against.

    apply plugin: eclipsebuild.BuildDefinitionPlugin

    eclipseBuild {
        eclipseVersion = '37'
    }

This configuration can be overridden through the `-Peclipse.version=<version>` Gradle property.

### BundlePlugin

The `BundlePlugin` Gradle plugin knows how to build Eclipse bundles. It extends the Java plugin and configures the project based
on the Eclipse descriptors. When applied, it configures to build the resources based on the content of the _build.properties_
file. Once built, the bundle manifest file _META-INF/MANIFEST.MF_ is copied and the version is replaced with the one configured
in the containing Gradle project.

The `BundlePlugin` also adds the capability to bundle jars along with the dependencies from the target platform. This
is enabled by declaring a `bundled` dependency.

    apply plugin: eclipsebuild.BundlePlugin

    dependencies {
        compile "eclipse:org.eclipse.core.runtime:+"
        bundled "com.google.guava:guava:+"
    }

The (transitive) dependencies of the `bundled` configuration are used by the `updateLibs` task which downloads the jars into
the _lib_ folder and updates the manifest file to export all packages of these these external dependencies.

### TestBundlePlugin

The `TestBundlePlugin` Gradle plugin is an extension of the `BundlePlugin` and knows how to run tests.

    apply plugin: eclipsebuild.TestBundlePlugin

    eclipseTest {
        fragmentHost 'com.gradleware.tooling.eclipse.core'
        applicationName 'org.eclipse.pde.junit.runtime.coretestapplication'
        optionsFile file('.options')
    }

The plugin adds a task called `eclipseTest`. This task does the following:

1. Clears the _buildDir/eclipseTest_ folder in order to have a clean environment for testing.
2. Copies the target platform to the _buildDir/eclipseTest_ folder.
3. Installs the project dependency plugins into the target platform with the _P2 director_.
4. Executes the target platform and runs the tests with a custom Gradle test runner (which is an `ITestRunListener` implementation).

The test results are collected and can be reviewed from the build/reports folder.

Many other things that depend on Eclipse will have to be tested in the context of a running Eclipse application. The current idea is
to run these IDE tests using [fragments](http://wiki.eclipse.org/FAQ_What_is_a_plug-in_fragment%3F) or plugins and to test similarly
as described in [PDE Unit Tests using Ant](http://www.eclipse.org/articles/article.php?file=Article-PDEJUnitAntAutomation/index.html).

### FeaturePlugin

The `FeaturePlugin` Gradle plugin knows how to build an Eclipse feature. It comes with a very simple DSL.

    apply plugin: eclipsebuild.FeaturePlugin

    feature {
        featureXml = file('feature.xml')
    }

The result is a jar containing all elements defined in the DSL and in the project's _build.properties_.

### UpdateSitePlugin

The `UpdateSitePlugin` Gradle plugin knows how to build an update site. It adds a task called `createP2Repository`. This task
takes all project dependencies and, if they are either Eclipse plugins or features, publishes them to a local P2 repository. The
generated repository is available in the _build/repository_ folder.

    apply plugin: eclipsebuild.UpdateSitePlugin

    updateSite {
        siteDescriptor = file("category.xml")
    }

### Adding a new Eclipse plugin

* Create a new folder under the _buildship_ root folder
* Create a project in that folder through Eclipse and use the same name for the project as for the folder
* Create a _build.gradle_ file and apply the `BundlePlugin`
* Add the project to the _settings.gradle_ file
