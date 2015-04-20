# Tooling Commons

## Implement model persistence in Tooling Model

### Requested Change

Persist the loaded models of the Tooling Model on disk such that the next time the Tooling Model is started, the
models are initialized with the persisted models.

### Motivation

In order not to slow down opening a workspace that contains Eclipse Gradle projects, the classpath entries of each
Eclipse Gradle projects need to be initialized with the values from the last time the projects were opened.


## Add null constraints to Javadoc of Omni models

### Requested Change

Enhance Javadoc to document when null is a valid return value, either through a parameter comment or
through the @Nullable annotation.

### Motivation

This is useful information to the developer.


## Reload the Gradle version when a model is forced to be reloaded

### Requested Change

When we _force_reload_ a model, also _force_reload_ the `BuildEnvironment` model.

### Motivation

The changed behavior will be more consistent.


