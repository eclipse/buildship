/**
 * Copyright (c) 2014  Andrey Hihlovskiy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 */

package eclipsebuild.mavenize

import java.util.jar.JarFile
import java.util.jar.Manifest
import org.osgi.framework.Constants

/**
 * Converts bundle manifest to POM.
 */
final class Bundle2Pom {
    String group
    String dependencyGroup

    /**
     * Constructs Bundle2Pom object with the specified parameters.
     * @param group - maven group to which the given artifact belongs.
     * @param dependencyGroup - maven group to which the dependencies of the given artifact belong.
     */
    Bundle2Pom(String group = null, String dependencyGroup = null) {
        this.group = group
        this.dependencyGroup = dependencyGroup
    }

    /**
     * Converts bundle to POM.
     * @param bundleFileOrDirectory - jar-file or directory containing OSGi bundle.
     * @return the converted POM.
     */
    Pom convert(File bundleFileOrDirectory) {
        def pom = new Pom()
        def manifest
        if (bundleFileOrDirectory.isDirectory()) {
            new File(bundleFileOrDirectory, 'META-INF/MANIFEST.MF').withInputStream {
                manifest = new Manifest(it)
            }
            pom.packaging = 'dir'
        } else
            manifest = new JarFile(bundleFileOrDirectory).manifest

        pom.artifact = manifest.attr.getValue(Constants.BUNDLE_SYMBOLICNAME)
        if (pom.artifact.contains(';'))
            pom.artifact = pom.artifact.split(';')[0]
        pom.artifact = pom.artifact.trim()

        pom.group = group ?: pom.artifact
        pom.dependencyGroup = dependencyGroup

        // cut the qualifier and use only the major.minor.service segments as version number
        // this is in sync with version constraints declared in the bundle manifest
        def version = new Version(manifest.attr.getValue(Constants.BUNDLE_VERSION))
        pom.version = "${version.major}.${version.minor}.${version.release}"

        parseDependencyBundles(pom.dependencyBundles, manifest.attr.getValue(Constants.REQUIRE_BUNDLE))

        return pom
    }

    private DependencyBundle parseDependencyBundle(String string) {
        List elements = string.split(';')
        String name = elements[0]
        elements.remove(0)
        DependencyBundle bundle = new DependencyBundle(name: name, resolution: Constants.RESOLUTION_MANDATORY, visibility: Constants.VISIBILITY_PRIVATE, version: "[1.0,)")
        for(String element in elements)
            if (element.startsWith(Constants.BUNDLE_VERSION_ATTRIBUTE)) {
                String s = element.substring(element.indexOf('=') + 1)
                if(s.startsWith('"'))
                    s = s.substring(1)
                if(s.endsWith('"'))
                    s = s.substring(0, s.length() - 1)
                bundle.version = s
            } else if (element.startsWith(Constants.RESOLUTION_DIRECTIVE))
                bundle.resolution = element.substring(element.indexOf('=') + 1)
            else if (element.startsWith(Constants.VISIBILITY_DIRECTIVE))
                bundle.visibility = element.substring(element.indexOf('=') + 1)
        return bundle
    }

    private void parseDependencyBundles(List<DependencyBundle> depBundles, String depBundlesString) {
        if(!depBundlesString)
            return
        int startPos = 0
        boolean quoted = false
        for(int i = 0; i < depBundlesString.length(); i++) {
            char c = depBundlesString.charAt(i)
            if(c == ',' && !quoted) {
                depBundles.add(parseDependencyBundle(depBundlesString.substring(startPos, i)))
                startPos = i + 1
            } else if(c == '"')
                quoted = !quoted
        }
        if(startPos < depBundlesString.length())
            depBundles.add(parseDependencyBundle(depBundlesString.substring(startPos, depBundlesString.length())))
    }
}

