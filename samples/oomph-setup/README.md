# Buildship sample: Oomph project setup script

This project shows how to use the Gradle Import Task in a Oomph Project Setup model.

# Installation and usage
To test the Setup model add open eclipse installer and add the project setup as a user project.
Oomph will import an example gradle project using Buildship.

# Testing against the local checkout
By default the setup tasks uses the latest released Buildship version from the update site. The eclipse
installer can be instructed to use the local site and model by the eclipse installer with the following parameters (e.g. via eclipse-inst.ini):

```ini
-Doomph.redirection.buildship=https://raw.githubusercontent.com/eclipse/buildship/master/buildship.setup->file:/<path to buildship checkout>/buildship.setup 
```

Windows has no eclipse-inst.ini, do this instead:

```
.\eclipse-inst-jre-win64.exe -vmargs '-Doomph.redirection.buildship=https://raw.githubusercontent.com/eclipse/buildship/master/buildship.setup->file:/<path to buildship checkout>/buildship.setup'
```

Replace `<path to buildship checkout>` with the location of your buildship checkout.
On the variables page, enter the URL of your fork, `https://github.com/<user>/buildship`, and the branch you want to checkout. This step can be skipped, if you only want to test changes to your `buildship.setup` (the local version will be used). 
To redirect Eclipse to use the local `buildship.setup`, use `-Dbuildship.oomphtest.enabled=true`. It will add another variable `buildship.oomphtest.setupLocation`. Set it to `<path to buildship checkout>/buildship.setup`. 
Without this configuration, the new Eclipse installation, will perform the setup tasks using the `buildship.setup` from the update site.