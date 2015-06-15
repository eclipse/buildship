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

import groovy.xml.MarkupBuilder
import org.osgi.framework.Constants

/**
 * POJO class holding data extracted from bundle and needed for POM generation.
 */
public final class Pom {
   private static final String encoding = 'UTF-8'

    String group
    String artifact
    String version
    String packaging = 'jar'
    List<DependencyBundle> dependencyBundles = []
    String dependencyGroup

    String toString() {
        ByteArrayOutputStream stm = new ByteArrayOutputStream()
        writeTo(new OutputStreamWriter(stm, encoding))
        String result = stm.toString(encoding)
        if(result.charAt(0) == 0xfeff)
            result = result.substring(1) // remove BOM, if present
        return result
    }

    void writeTo(Writer writer) {
        def builder = new MarkupBuilder(writer)
        builder.mkp.xmlDeclaration(version: '1.0', encoding: encoding)
        def pom = this
        builder.project xmlns: 'http://maven.apache.org/POM/4.0.0', 'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance', {
            modelVersion '4.0.0'
            if(pom.group)
                groupId pom.group
            if(pom.artifact)
                artifactId pom.artifact
            if(pom.version)
                version pom.version
            if(pom.packaging != 'jar')
                packaging pom.packaging
            if(pom.dependencyBundles)
                dependencies {
                    for(def depBundle in pom.dependencyBundles)
                        dependency {
                            if(depBundle.group)
                                groupId depBundle.group
                            else if(dependencyGroup)
                                groupId dependencyGroup
                            else
                                groupId depBundle.name
                            artifactId depBundle.name
                            if(depBundle.version)
                                version depBundle.version
                            scope 'compile'
                            if(depBundle.resolution == Constants.RESOLUTION_OPTIONAL)
                                optional true
                        }
                }
        }
    }
}

