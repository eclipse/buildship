package eclipsebuild.jar

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency

import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class JarBundleUtils {

    static File firstDependencyJar(Configuration configuration) {
        ResolvedArtifact jarArtifact = findJarArtifact(getResolvedDependency(configuration))
        jarArtifact.file
    }

    private static ResolvedDependency getResolvedDependency(Configuration configuration) {
        configuration.resolvedConfiguration.firstLevelModuleDependencies.first()
    }

    private static ResolvedArtifact findJarArtifact(ResolvedDependency dependency) {
        dependency.moduleArtifacts.find { it.extension == 'jar' }
    }

    static String manifestContent(File jar, String template, String packageFilter, String bundleVersion, String qualifier, String sourceReference) {
        List<String> packageNames = packageNames(jar, packageFilter) as List
        packageNames.sort()
        String fullVersion = "${bundleVersion}.${qualifier}"
        manifestFor(template, packageNames, bundleVersion, fullVersion, sourceReference)
    }

    private static Set<String> packageNames(File jar, String filteredPackagesPattern) {
        def result = [] as Set
        Pattern filteredPackages = Pattern.compile(filteredPackagesPattern)
        new ZipInputStream(new FileInputStream(jar)).withCloseable { zip ->
            ZipEntry e
            while (e = zip.nextEntry) {
                if (!e.directory && e.name.endsWith(".class")) {
                    int index = e.name.lastIndexOf('/')
                    if (index < 0) index = e.name.length()
                    String packageName = e.name.substring(0, index).replace('/', '.')
                    if (!packageName.matches(filteredPackages)) {
                        result.add(packageName)
                    }
                }
            }
        }
        result
    }

    private static String manifestFor(String manifestTemplate, Collection<String> packageNames, String mainVersion, String fullVersion, String sourceReference) {
        StringBuilder manifest = new StringBuilder(manifestTemplate)

        if (!packageNames.isEmpty()) {
            String exportedPackages = packageNames.collect { " ${it};version=\"${mainVersion}\"" }.join(',\n')
            manifest.append "Export-Package:${exportedPackages}\n"
        }
        manifest.append "Bundle-Version: $fullVersion\n"
        if (sourceReference) {
            manifest.append "Eclipse-SourceReferences: $sourceReference\n"
        }
        manifest.toString()
    }
}
