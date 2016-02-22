# Frequently Asked Questions

### Q. Importing my Gradle project with Buildship downloads a Gradle distribution, even though the distribution was already downloaded previously when building the project with the Gradle Wrapper from the command line. Does Buildship use a different Gradle distribution cache?

__A.__ No, if the Gradle Wrapper is specified during the import, then Buildship uses the same Gradle distribution cache as
the command line. However, depending on which target Gradle version is used, there might be some differences. The wrapper
contains an archive called `gradle-wrapper.jar` which is created along with `gradlew`. Buildship uses the latest version
of the archive. The wrapper script on the other hand has the version of Gradle with which the script was generated.

The `gradle-wrapper.jar` archive contains - amongst other things - the logic to calculate where to download the Gradle
distribution from. The location calculation has [changed](https://github.com/gradle/gradle/commit/2e6659547e71bb3fca1c952d823ec660433ab5d9) in
Gradle version 2.2. Consequently, Buildship does not find Gradle distributions that have been previously downloaded
from the command line if the target Gradle version is <2.2 and downloads them again. Apart from this limitation, Buildship
reuses cached Gradle distributions.

If you are interested in the discussion of this topic, check out the issue in [Bugzilla](https://bugs.eclipse.org/bugs/show_bug.cgi?id=468466).


### Q. Is it possible to disable the dependency management for a project?

__A.__ Not from the UI, because the Gradle project configuration is always kept in sync with the Eclipse project. It can be done by adding the following snippet to the build script:

    apply plugin: 'eclipse'
    eclipse.classpath.plusConfigurations = []
