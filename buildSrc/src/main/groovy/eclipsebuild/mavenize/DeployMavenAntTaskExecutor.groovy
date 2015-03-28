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

package eclipsebuild.mavenize

import org.apache.maven.artifact.ant.Mvn;
import org.gradle.api.Project;

import groovy.xml.NamespaceBuilder

import java.util.Map

/**
 * Deploys OSGI bundle (jar or directory) to maven repository
 */
class DeployMavenAntTaskExecutor {

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

    final AntBuilder ant
    final File target
    final File workFolder

    /**
     * Constructs Deployer with the specified parameters.
     */
    DeployMavenAntTaskExecutor(AntBuilder ant, File target) {
        this.ant = ant
        this.ant.taskdef(resource: 'org/apache/maven/artifact/ant/antlib.xml', classpath: Mvn.class.getProtectionDomain().getCodeSource().getLocation())
        this.target = target
        this.workFolder = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString())
        this.workFolder.deleteOnExit()
    }

    /**
     * Deploys the specified bundle with the specified POM to target maven repository.
     * @param options - may contain sourceFile (of type java.io.File), pointing to sources jar.
     * @param pomStruct - contains POM that will be used for deployment
     * @param bundleFileOrDirectory - jar-file or directory, containing OSGI bundle
     */
    void deployBundle(Map options = [:], Pom pomStruct, File bundleFileOrDirectory) {
        workFolder.mkdirs()
        def pomFile = new File(workFolder, 'myPom.xml')
        File bundleFile
        if (bundleFileOrDirectory.isDirectory()) {
            pomStruct.packaging = 'jar'
            pomFile.text = pomStruct.toString()
            File zipFile = new File(workFolder, "${pomStruct.artifact}-${pomStruct.version}.jar")
            ant.zip(basedir: bundleFileOrDirectory, destfile: zipFile)
            bundleFile = zipFile
        }
        else {
            pomFile.text = pomStruct.toString()
            bundleFile = bundleFileOrDirectory
        }
        File sourceFile = options.sourceFile
        if(sourceFile?.isDirectory()) {
            File zipFile = new File(workFolder, sourceFile.name + '.jar')
            ant.zip(basedir: sourceFile, destfile: zipFile)
            sourceFile = zipFile
        }
        ant.with {
            pom id: 'mypom', file: pomFile
            deploy file: bundleFile, {
                pom refid: 'mypom'
                if(sourceFile)
                    attach file: sourceFile, type: 'jar', classifier: 'sources'
                remoteRepository url: this.target.toURI().toURL().toString(), {
                }
            }
        }
    }
}

