# Buildship sample: Custom Tooling API model

This project shows how can an Eclipse plugin load custom Gradle models via the Buildship's `GradleBuild.withConnection()` API.

The custom model loading implementation is based on https://github.com/bmuschko/tooling-api-custom-model.
For the details check the documentation there.

# Installation and usage
To try the plugin out, clone the repository, open `site.xml` in `org.eclipse.buildship.sample.custommodel.site`, click `Build All` and install the sample plugin from the generated update site.

If the installation is successful then the plugin contributes a new action to the toolbar with the Gradle logo.
Clicking on it will display the `java` plugin application for all Gradle builds in the workspace.