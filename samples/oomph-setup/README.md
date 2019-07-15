# Buildship sample: Oomph project setup script

This project shows how to use the Gradle Import Task in a Oomph Project Setup model.

# Installation and usage
To test the Setup model add open eclipse installer and add the project setup as a user project.
Oomph will import an example gradle project using Buildship.

# Testing against the local checkout
By default the setup tasks uses the latest released Buildship version from the update site. The eclipse
installer can be instructed to use the local site and model by building it using `gradlew :org.eclipse.buildship.site:build` and
supplying the eclipse installer with the following parameters (e.g. via eclipse-inst.ini):

```ini
-Doomph.redirection.bs=https://raw.githubusercontent.com/eclipse/buildship/master/org.eclipse.buildship.oomph/model/GradleImport-1.0.ecore->file:/<path to builship checkout>/org.eclipse.buildship.oomph/model/GradleImport-1.0.ecore
-Doomph.redirection.p2.buildship=https://download.eclipse.org/buildship/updates/e411/releases->file:/<path to builship checkout>/org.eclipse.buildship.site/build/repository
```

Replace `<path to builship checkout>` with the location of your buildship checkout.