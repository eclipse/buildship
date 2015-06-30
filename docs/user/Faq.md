# Frequently Asked Questions

### Q. Importing my project with Buildship downloads a Gradle distribution, even though it was already downloaded previously, when built with the wrapper from the command line. Does Buildship use a different cache?

__A.__ No, if the Gradle wrapper is specified during the import, then Buildship uses the same cache as the command line. However, depending on which Gradle version is used, there might be some differences. The wrapper contains an archive called `gradle-wrapper.jar` which is created along with `gradlew`. Buildship uses the latest version of it. The wrapper script on the other hand has the version from its generator Gradle version.

This archive contains - amongst others - the location calculation where to download the gradle distribution. [The calculation has changed in Gradle version 2.2](https://github.com/gradle/gradle/commit/2e6659547e71bb3fca1c952d823ec660433ab5d9). Consequently, Buildship won't find the previously downloaded Gradle version and fetches a new one.

Apart from this quirk Buildship reuses the caches and should operate as expected. If you are interested the discussion on this topic check out [this bugzilla](https://bugs.eclipse.org/bugs/show_bug.cgi?id=468466).
