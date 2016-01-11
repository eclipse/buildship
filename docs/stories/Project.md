# Project import and configuration

## Add option to clear Eclipse descriptors upon project import

### Estimate

1.5 days

### Requested change

During the project import if one or more modules contain a .project file, then the user should be notified. He can decide whether or not these descriptors should be deleted.

### Motivation

Users are reportedly find it confusing that there is a difference between importing projects with or without Eclipse
project descriptors.

### Implementation plan
- Add `deleteDescriptors` boolean argument to the `WorkspaceGradleOperations.synchronizeGradleBuildWithWorkspace()` and the `WorkspaceGradleOperations.synchronizeGradleBuildWithWorkspace()` methods. If true, delete the Eclipse descriptors. For project refresh `deleteDescriptors` is always false.
- Extend `SynchronizeGradleProjectJob` to optionally search for existing project descriptors. If the search is enabled and some descriptors exists, then show a dialog to the user whether to delete descriptors. If the user selects a clean import, then call `WorkspaceGradleOperations.synchronizeGradleBuildWithWorkspace()` with `deleteDescriptors` set to true.
- Enable the descriptor search feature in `ProjectImportWizardController#performProjectImport()`

### Test cases

- Imported project has a .project file
- .project file under the project folder is malformed (invalid xml)
- Subset of the modules in a multi-project build has project descriptors


## Fine-tune import wizard UI

### Requested Change

Based on UX feedback, improve the import wizard screens. Primarily, this means providing more context information
along each step of the wizard.

### Motivation

Using the import wizard must be as intuitive, informative, and self-explanatory as possible.


## Allow to configure Java home through dropdown

### Requested Change

Rather than displaying a text field and a file chooser to configure the Java home, allow the user to choose from
the installed runtime JREs of the current workspace through a dropdown.

### Motivation

We want to provide a deep integration into Eclipse, allowing users the means to configure a JRE the way they already know.


## Support import of Buildship into Buildship

### Requested Change

In order to allow development of Buildship with Buildship, the import logic needs to become Eclipse plugin-aware.

1. Read the JRE version from the MANIFEST file and set on the project.
1. Detect if the project uses the Groovy plugin and set the Groovy nature for those projects.

### Motivation

Buildship should support the development of Eclipse plugins written in Java.


## ~~Support re-importing a project~~

### Requested Change

When importing a Gradle project that has already been imported into the workspace as a Gradle project before, override
the existing configuration rather than failing with an error that the project already exists. During the re-import, detect
if something has actually changed before applying the change.

### Motivation

It is a common scenario for users to re-import a project because its configuration has changed.


## Supported nested projects feature of Eclipse Mars

### Requested Change

For the Eclipse Mars distribution, display the Eclipse projects of a Gradle multi-project build in a hierarchy. Have an option
in the header of the Project Explorer to switch between flat view and nested view.

### Motivation

Displaying the Eclipse projects in a hierarchy closely reflects the Gradle project hierarchy of a multi-project build.


## Support partial imports

### Requested Change

It should probably be possible to import only a single Gradle project from a multi-project build.

### Motivation

It seems a very common use case for Eclipse developers to only import part of a multi-project build.


## Provide option to convert an Eclipse project to Gradle

### Requested Change

Similar to other components like PDE or Maven, Buildship should allow to convert an existing project via the context menu of
the project, i.e., Configure -> Convert to Gradle Project.

The most simple would be to add the Gradle nature to the project and to create an empty Gradle build file.

A more invested solution would be to check if the Java nature is available on the project and add the Java plug-in to the
generated Gradle build file in this case. Also if the source folder follows the 'legacy' structure of Eclipse projects, a source
mapping should be added to the Gradle build file.

See [Bug 465355](https://bugs.eclipse.org/bugs/show_bug.cgi?id=465355) for details on the implementation.

### Motivation

This development would make a migration to Gradle simpler for new users and follows the Eclipse conventions of conversions.


## Import buildSrc into workspace

### Requested Change

If available, import the _buildSrc_ folder into the workspace as a separate Eclipse project named _buildSrc_.

### Motivation

Build authors often make use the _buildSrc_ folder in non-trivial projects.


## ~~Project creation wizard~~

### Requested Change

Provide a wizard that allows to configure the main aspects of a single-/multi-project build. Create the involved build
files, configure them according to what the user has specified, and then import the newly created project into the workspace.

### Motivation

Inexperienced Gradle users appreciate a project creation wizard that provides an easy way to get started with a new Gradle project.


## Verify project import settings upon project synchronization 

### Requested Change
Make the project import validation mechanism part of the the project synchronization mechanism.

### Motivation

In Eclipse it is common practice to import projects with existing Eclipse descriptors by using the 'Import Existing Project' wizard.
Currently it is broken for Buildship because the `.settings/gradle.prefs` file contain absolute values specific a local
environment. If the project import settings verification was part of the project synchronization mechanism, then it would be 
possible to validate and fix without the import wizard. Also, if the imported project doesn't have the settings file, we could generate
one with sensible defaults. Moreover, we could get rid of the `GradleProjectValidationResourceDeltaVisitor` class which was reported
hundreds of times via the [AERI](https://dev.eclipse.org/recommenders/committers/confess/#/problems/55d448e2e4b0f0b83a6e47ab/details) 
(Eclipse committer account required).
