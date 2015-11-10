# Web Tools

# Provide Web Tools Integration for Buildship

## Higher-level goals
- Configure web application directory for deployment

    Purpose: The web application directory contains all main web application resources, excluding compiled Java classes and non-compiled resources necessary for those Java classes to run. This folder contains resources such as web pages, images, JavaScript and CSS assets. Upon deployment, this folder is considered the default root. For example, if the ‘myapp’ project is deployed as myapp.war, then the src/main/webapp folder would be deployed at the root of the archive.

- Add Java and Dynamic Web Facet to project

    Purpose:

    _Java Facet_: The Java facet allows the user to more easily set a project’s Java version, and also acts as a version constraint for other facets. For instance, the Dynamic Web facet with version 3.0 requires a Java facet with a version of at least 1.7. The Java Facet is necessary, as the Dynamic Web Facet depends on the existence of the Java facet.
    
    _Dynamic Web Facet_: The Dynamic Web Facet is necessary, as it is required by server adapters. Without the Dynamic Web facet, it wouldn’t be possible to deploy a project. The Dynamic Web Facet adds a /WebContent web application directory by default. All the deployment configuration is configured under .settings/org.eclipse.wst.common.component. The Web Facet enables different Web validators on the project. Additionally the Javascript Facet is automatically added along the Web Facet, which in turn enables javascript validators.

- Add Utility Facet to deployable project dependencies

    Purpose: The Utility facet indicates to a server adapter that the project can be deployed as a jar. Project dependencies that are listed in a web application that do not have the utility facet cannot be deployed.

- Customize the Gradle container so that each classpath entry deploys or not, as defined by the gradle

    Purpose: Certain dependencies shouldn’t be deployed, such as test dependencies and provided dependencies. The classpath entries, and classpath container entries, need to be marked as deployable or non-deployable. During deployment, if the dependency is an archive, then it is copied into the runtime-path of the deployment area. If it’s a project, an archive will be assembled, depending on the .component configuration of that project, then copied over the runtime path.

## Story - Define internal project configuration API

### Motivation

Ad hoc functionality will be defined by plugins separate from Buildship core, but will need to be invoked by Buildship core.

### The (internal) API

The `IProjectConfigurator` will be the interface that is implemented by all configurators that add an extension to the extension point.
- canConfigure determines whether the configurator should be applied
- configure invokes the configurators configuration routine
- getWeight returns this configurators weight. Configurators may have prerequisite configurators, and as such Buildship needs to know the order to apply these configurators. (lower weight = greater priority)

```
interface IProjectConfigurator {
    boolean canConfigure(ProjectConfigurationRequest);
    IStatus configure(ProjectConfigurationRequest);
    int getWeight();
}
```

The `ProjectConfigurationRequest` class contains all information necessary for project configuration. The purpose of having this class is to make the api more extensible.

```
class ProjectConfigurationRequest {
    IProject getWorkspaceProject();
    OmniEclipseProject getProject();
    WorkspaceOperations getWorkspaceOperations();
}
```

### Implementation

- Define extension point
- Define schema
- Define `IProjectConfigurator` interface
- Define `ProjectConfigurationRequest` class
- Implement project configurator API in Buildship project synchronization
    - Add extension point reader
    - Apply all configurators that should be applied to given project

### Test Cases

- Configurator can be invoked by using extension point
- Configurator only applied if `canConfigure` returns true
- Configurator weight respected (lesser weight configured first)

### Open Questions

The test cases seem to be dependent on the existence of a configurator, and the configurator is dependent on the existence of the internal configurator API.
Should the test cases be implemented after the two separate stories are complete?

What should Buildship do upon an error in a configurator?

## Story - Define WTP plugins and features

### Motivation

Buildship core is meant to be a plugin for the Java distribution of Eclipse. A WTP component of Buildship will require several WTP dependencies, none of which are required by a developer using the Java distribution. Thus, the WTP component will need to be loosely coupled with Buildship Core. The proposed implementation defines a separate WTP plugin, test plugin, and feature. Users who wish to use the WTP component won't be able to get it from Buildship core, but will rather be able to download it as a separate plugin.

### Implementation

- Wait for "Expose web application directory" story in TAPI to be completed
- Wait for "Define internal project configuration API" story to be completed
- Define `org.eclipse.buildship.wtp` plugin
- Define `org.eclipse.buildship.wtp.test` plugin
- Define `org.eclipse.buildship.wtp.feature`
- Define extension
- Add WTP configurator class

### Test Cases
- Configuration only invoked on web application projects

Once WTP configuration is complete, we can add the feature to the update site. 

### Open questions

If a configurator returns an error in the IStatus, should the configurator operations be rolled back? Should we have some sort of `rollback` function that a configurator must implement?

## Story - Add Java Facet and Dynamic Web Facet to project

### Motivation

The Dynamic Web Facet is required by a WTP project for deployment. The Java Facet is required by the Dynamic Web Facet due to a JavaEE facet constraint. Both of these facets should be added to a web application project.

### Implementation
- Wait for "Expose web application directory" story in TAPI to be completed
- Wait for 'Define WTP plugins and features' story to be completed
- Add facet configuration to WTP configurator

### Test Cases

- Java facet is added to project
- Dynamic Web Facet is added to project
- If the Java Facet is already on a project but has incorrect version, the version is updated to correct version based on source Java version
- If the Dynamic Web Facet is already on project but has incorrect version, the version is updated to correct version
- Default Dynamic Web Facet version is 2.5.
- If Java Facet exists on project with correct version, Facet is not affected.
- If Dynamic Web Facet exists on project with correct version, Facet is not affected.
- Dynamic Web Facet version inferred from _web.xml_ file.

### Open Questions

- The Java Facet relies on information about the source/target Java versions. Should this story be blocked by the [Java source level story](https://github.com/eclipse/buildship/blob/master/docs/stories/ToolingAPI.md#set-source-level-for-java-projects)?

- Should the version from the web.xml file be exposed through the tooling API?

## Story - Configure web application directory in WTP configurations for web application projects

### Motivation

The `ide-integration.md` document in Gradle core specifies a story ["Expose web application directory"](...). The goal is to communicate the location of the web application directory to WTP.

### Implementation

- Wait for "Expose web application directory" story in TAPI to be completed
- Wait for 'Add Java Facet and Dynamic Web Facet to project' story to be completed
- Add web app dir configuration to configurator class

### Test Cases

- Custom web application directory location is communicated successfully to component file, file exists.
- Default web application directory location is communicated successfully to component file, file exists.
- Custom web application directory location is communicated successfully to component file, but file does not exist
- Default web application directory location is communicated successfully to component file, but file does not exist
- Existing `wb-resource` entries in component file not overwritten.

### Open Questions

The testcase on `wb-resources` being persisted is based on the assumption that we have no way to determine whether a `wb-resource` entry is added manually by a user, or added by Buildship.
Are we able to add attributes to the `wb-resource`?

### Notes

The default Dynamic Web Facet version is 2.5, as it allows for a missing web.xml file, and doesn't have a strong constraint on the Java version (at least Java 5).

