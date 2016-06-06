# Frequently Asked Questions

### Q. Running Java compilation tasks fails with `Could not find tools.jar`, how can I fix this?

By default, Buildship uses whatever Java runtime you started Eclipse with. If that runtime is only a JRE, then no compiler is available. We recommend you specify your Java home in the `~/.gradle/gradle.properties` file, using the `org.gradle.java.home` property. This property will be honored both when running from Eclipse and when running from the command line.

If you want to cross-compile against different Java versions, put a property into your `~/.gradle/gradle.properties` file for each Java version you need:

```
jdk6Home=/some/path
jdk7Home=/some/other/path
```

In your Gradle builds you can use these variables to set up the compiler like this:

```
tasks.withType(JavaCompile) {
    options {
            fork = true
            bootClasspath = "${jdk7Home}/jre/lib/rt.jar"
    }
}
```

### Q. How can I customize which dependencies are in the Gradle classpath container?

__A.__ : Buildship consumes the dependencies defined in your build script. By default the `compileOnly`, `runtime`, `testCompileOnly` and `testRuntime` dependencies are added. This can be changed in the `eclipse.classpath` model. 

The following snippet will add the `custom` dependency scope to your Eclipse classpath:

    apply plugin: 'eclipse'
    
    configurations {
        custom
    }
    
    eclipse.classpath.plusConfigurations << custom


And this one will remove all dependencies:

    apply plugin: 'eclipse'
    eclipse.classpath.plusConfigurations = []
    
### Q. Can my build folder be outside of the project?

__A.__ Yes, but unlike with source folders, you have to set up the link yourself at the moment:

    apply plugin: 'eclipse'
    buildDir = "../some-other-place"
    eclipse.project.linkedResource name:'build', type:'2', location: file('../some-other-place').path
