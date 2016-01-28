# General

## Provide project/workspace settings

### Requested Change

Integrate into the workspace settings such that the user can define workspace-wide and project-specific settings, like
whether to show the Build Progress View when running a Gradle task.

### Motivation

Currently, the default behavior is either configured during the import of a project or some smart defaults are applied
by Buildship itself. It must also become possible for the user to actively control and override these configuration settings.

## Remove absolute paths from gradle.prefs

### Requested Change

All paths in the gradle.prefs file should be relative, so that the file can be shared among developers.

### Motivation

Sharing the preferences would allow Buildship projects to be imported using the default "Import Existing Projects" wizard, which supports bulk-importing an arbitrary number of projects. It also makes checking out from version control simpler, as the user can use the "Existing Projects" option instead of re-running the Gradle Import Wizard.

### Implementation Plan

#### Remove `project_dir`

The gradle.prefs are always read in the context of an `IProject` instance, which already has a `getLocation()` method.
The `project_dir` preference is thus redundant. The `projectDir` property of the `ProjectConfiguration` can
instead be filled with the value taken from the `IProject`. This makes the change transparent to clients of the configuration.

#### Persist `connection_project_dir` as a relative path

Since we know the `getLocation()` of the current `IProject`, we can store the path to the connection project (a.k.a. root project) as a relative path. For example, if a subproject is nested in its root project like this:

```
  root
    sub
```

Then the gradle.prefs for the the `sub` project would contain the following line:

```
  "connection_project_dir" : "../"
```

For a flat layout like this:

```
  root
  sub
```

the gradle.prefs for the the `sub` project would contain the following line:

```
  "connection_project_dir" : "../root/"
```

This relativization would be hidden in the persistence layer, so the connection project directory returned by the `ProjectConfiguration` would return an absolute path. This again means that clients can remain unchanged.

#### Migrate old absolute paths to relative paths

The persistence logic will still support absolute paths when reading the preferences, but will use relative paths when storing them again. This means that the "Refresh Gradle Project" action can be used to upgrade the configuration to the new format.

#### Test cases

- all existing Buildship tests (excluding those testing the persistence directly) must pass unchanged
- project with a nested layout is persisted correctly
- project with a flat layout (subprojects next to parent project) is persisted correctly
- old settings files with absolute paths are still supported
- reading an old, absolute configuration and persisting it again creates relative paths
