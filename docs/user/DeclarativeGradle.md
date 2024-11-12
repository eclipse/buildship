# Declarative Gradle - EAP

Our [Declarative Gradle](https://declarative.gradle.org/) attempts to create an elegant and extensible declarative build language that enables expressing any build in a clear and understandable way.
Declarative uses a new `*.gradle.dcl` format, which resembles a simplified Kotlin language with some unique features.

Buildship provides some EAP support for the Declarative Gradle language by using our [declarative-lsp](https://github.com/gradle/declarative-lsp) project.

## Setup

In order to install

1. In the Help > Install New Software dialog, add the following update site: https://download.eclipse.org/buildship/updates/latest-snapshot/
1. Install the "Buildship - Gradle Declarative editor support" under the "Buildship Extras - Incubating" category.
1. Check out and build the [declarative-lsp](https://github.com/gradle/declarative-lsp) project by running `shadowJar`. The task should make a `lsp-all.jar` file under `lsp/build/libs`.
1. Go to the "Settings > Gradle > Experimental features" menu, and select this `lsp-all.jar` file.
1. Upon opening any `gradle.dcl` file, the LSP should turn on. Observe the console for any diagnostic messages.

## Sample projects

To try out the feature, take a look at our [sample projects](https://declarative.gradle.org/docs/getting-started/samples/).
These projects should be importable by Buildship without any problem, but make sure that the importing uses the wrapper shipped by the samples.

## Verifying if the LSP works

Eclipse offers a "Language Servers" view.
If everything is set up correctly, upon opening a DCL file, a "Declarative Gradle Language Server" entry should show up in the list persistently.
