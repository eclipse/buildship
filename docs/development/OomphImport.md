# Testing buildship.setup
By default, Oomph will use the master version at: https://raw.githubusercontent.com/eclipse/buildship/master/buildship.setup.

The installer can be instructed to use a different version (e.g. via eclipse-inst.ini):

```ini
-Doomph.redirection.buildship=https://raw.githubusercontent.com/eclipse/buildship/master/buildship.setup->file:/<path to buildship checkout>/buildship.setup 
```

Windows has no eclipse-inst.ini, do this instead:

```
.\eclipse-inst-jre-win64.exe -vmargs '-Doomph.redirection.buildship=https://raw.githubusercontent.com/eclipse/buildship/master/buildship.setup->file:/<path to buildship checkout>/buildship.setup'
```

Replace `<path to buildship checkout>` with the location of your Buildship checkout.
On the variables page, enter the URL of your fork, `https://github.com/<user>/buildship`, and the branch you want to checkout. This step can be skipped, if you only want to test changes to your `buildship.setup` (the local version will be used). 
To redirect Eclipse to use the local `buildship.setup`, use `-Dbuildship.oomphtest.enabled=true`. It will add another variable `buildship.oomphtest.setupLocation`. Set it to `<path to buildship checkout>/buildship.setup`. 
Without this configuration, the new Eclipse installation, will perform the setup tasks using the `buildship.setup` from the update site.