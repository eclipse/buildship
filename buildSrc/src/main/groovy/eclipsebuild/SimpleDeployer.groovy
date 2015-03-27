/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package eclipsebuild

import org.apache.commons.codec.digest.DigestUtils
import org.akhikhl.unpuzzle.utils.IConsole
import org.akhikhl.unpuzzle.utils.SysConsole
import org.akhikhl.unpuzzle.eclipse2maven.EclipseSource
import org.akhikhl.unpuzzle.eclipse2maven.Version
import org.akhikhl.unpuzzle.osgi2maven.Pom
import org.akhikhl.unpuzzle.osgi2maven.Bundle2Pom
import org.akhikhl.unpuzzle.osgi2maven.DependencyBundle
import org.akhikhl.unpuzzle.osgi2maven.Deployer

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

/**
 * Converts an Eclipse plugin folder to a Maven repository.
 * <p/>
 * Note: this class has been taken from {@code org.akhikhl.unpuzzle:unpuzzle-plugin:0.0.17}
 * and modified.
 */
final class SimpleDeployer {

    // TODO (donat) cleanup the unused parts from this code

    private final File targetDir
    private final String eclipseGroup
    private final Deployer mavenDeployer
    private final IConsole console
    private final String installGroupPath
    private final String installGroupChecksum
    private Map artifacts = [:]
    private Map artifactsNl = [:]
    private Map artifactFiles = [:]
    private Map sourceFiles = [:]

    SimpleDeployer(File targetDir, String eclipseGroup, Deployer mavenDeployer, IConsole console = null) {
        this.targetDir = targetDir
        this.eclipseGroup = eclipseGroup
        this.mavenDeployer = mavenDeployer
        this.console = console ?: new SysConsole()
        installGroupPath = mavenDeployer.repositoryUrl.toString() + '/' + (eclipseGroup ? eclipseGroup.replace('.', '/') : '')
        installGroupChecksum = DigestUtils.md5Hex(installGroupPath)
    }

    private void collectArtifactsInFolder(EclipseSource source, artifactsSourceDir) {
        def processFile = { File file ->
            console.info("Collecting artifacts: ${file.name}")
            try {
                Bundle2Pom reader = new Bundle2Pom(group: eclipseGroup, dependencyGroup: eclipseGroup)
                Pom pom = reader.convert(file)
                def source_match = pom.artifact =~ /(.*)\.source/
                if(source_match) {
                    def artifact = source_match[0][1]
                    sourceFiles["${artifact}:${pom.version}"] = file
                } else if(!source.sourcesOnly) {
                    def nl_match = pom.artifact =~ /(.*)\.nl_(.*)/
                    if(nl_match) {
                        def artifact = nl_match[0][1]
                        def language = nl_match[0][2]
                        if(!artifactsNl[language])
                            artifactsNl[language] = [:]
                        artifactsNl[language][artifact] = pom
                    } else if(!source.languagePacksOnly) {
                        if(!artifacts.containsKey(pom.artifact))
                            artifacts[pom.artifact] = []
                        artifacts[pom.artifact].add pom
                    }
                    artifactFiles["${pom.artifact}:${pom.version}"] = file
                }
            } catch (Exception e) {
                console.info("Error while mavenizing ${file}")
                e.printStackTrace()
            }
        }
        console.startProgress("Reading bundles in $artifactsSourceDir")
        try {
            artifactsSourceDir.eachDir processFile
            artifactsSourceDir.eachFileMatch ~/.*\.jar/, processFile
        } finally {
            console.endProgress()
        }
    }

    void deploy(List<EclipseSource> sources) {
        for(EclipseSource source in sources) {
            File unpackDir = targetDir
            boolean packageInstalled = false

            if(!packageInstalled) {
                File pluginFolder = new File(unpackDir, 'plugins')
                if (!pluginFolder.exists()) {
                    pluginFolder = unpackDir
                }
                collectArtifactsInFolder(source, pluginFolder)
            }
        }

        fixDependencies()

        console.startProgress('Deploying artifacts')
        try {
            artifacts.each { name, artifactVersions ->
                artifactVersions.each { pom ->
                    mavenDeployer.deployBundle pom, artifactFiles["${pom.artifact}:${pom.version}"], sourceFile: sourceFiles["${pom.artifact}:${pom.version}"]
                }
            }
            artifactsNl.each { language, map_nl ->
                map_nl.each { artifactName, pom ->
                    mavenDeployer.deployBundle pom, artifactFiles["${pom.artifact}:${pom.version}"]
                }
            }
        } finally {
            console.endProgress()
        }
    }

    private void fixDependencies() {
        console.startProgress('Fixing dependencies')
        try {
            artifacts.each { name, artifactVersions ->
                console.info("Fixing dependencies: $name")
                artifactVersions.each { pom ->
                    pom.dependencyBundles.removeAll { reqBundle ->
                        if(!artifacts[reqBundle.name.trim()]) {
                            console.info("Warning: artifact dependency $pom.group:$pom.artifact:$pom.version -> $reqBundle.name could not be resolved.")
                            return true
                        }
                        return false
                    }
                    pom.dependencyBundles.each { reqBundle ->
                        def resolvedVersions = artifacts[reqBundle.name.trim()]
                        if(resolvedVersions.size() == 1)
                            reqBundle.version = resolvedVersions[0].version
                        else if(!resolvedVersions.find { it -> it.version == reqBundle.version.trim() }) {
                            def compare = { a, b -> new Version(a).compare(new Version(b)) }
                            resolvedVersions = resolvedVersions.sort(compare)
                            int i = Collections.binarySearch resolvedVersions, reqBundle.version.trim(), compare as java.util.Comparator
                            if(i < 0)
                                i = -i - 1
                            if(i > resolvedVersions.size() - 1)
                                i = resolvedVersions.size() - 1
                            def c = resolvedVersions[i]
                            def depsStr = resolvedVersions.collect({ p -> "$p.group:$p.artifact:$p.version" }).join(', ')
                            console.info("Warning: resolved ambiguous dependency: $pom.group:$pom.artifact:$pom.version -> $reqBundle.name:$reqBundle.version, chosen $c.group:$c.artifact:$c.version from [$depsStr].")
                            reqBundle.version = c.version
                        }
                    }
                    artifactsNl.each { language, map_nl ->
                        def pom_nl = map_nl[pom.artifact]
                        if(pom_nl)
                            pom.dependencyBundles.each { dep_bundle ->
                                def dep_pom_nl = map_nl[dep_bundle.name]
                                if(dep_pom_nl) {
                                    pom_nl.dependencyBundles.add new DependencyBundle(name: dep_pom_nl.artifact, version: dep_pom_nl.version, visibility: dep_bundle.visibility, resolution: dep_bundle.resolution)
                                }
                            }
                    }
                }
            }
        } finally {
            console.endProgress()
        }
    }

}

