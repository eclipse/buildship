package org.eclipse.buildship.core.util.file

import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import org.eclipse.core.runtime.Path

class RelativePathUtilsTest extends Specification {

    @ClassRule @Shared TemporaryFolder tempFolder

    // root
    // |- a
    //    |-b/c/file.txt
    //    |-d/e

    def setupSpec() {
        tempFolder.newFolder('a', 'b', 'c')
        tempFolder.newFolder('a', 'd', 'e')
        tempFolder.newFile('a/b/c/file.txt')
    }

    def "Relative path calculation throws exception for illegal arguments"() {
        when:
        RelativePathUtils.getRelativePath(base, target) == '..'

        then:
        thrown IllegalArgumentException

        where:
        base            | target
        null            | rootDir
        rootDir         | null
        rootDir         | txtFile
        txtFile         | rootDir
        new File('rel') | rootDir
        rootDir         | new File('rel')
    }

    def "Can calculate relative path"() {
        expect:
        RelativePathUtils.getRelativePath(base, target) == expected

        where:
        base | target | expected
        ADir | ADir   | "."
        ADir | BDir   | "b"
        BDir | ADir   | ".."
        CDir | ADir   | "..${sep}.."
        DDir | BDir   | "..${sep}b"
        EDir | BDir   | "..${sep}..${sep}b"
        EDir | CDir   | "..${sep}..${sep}b${sep}c"
    }

    def "Absolute path calculation throws exception for illegal arguments"() {
        when:
        RelativePathUtils.getAbsoluteFile(base, relativePath)

        then:
        thrown IllegalArgumentException

        where:
        base    | relativePath
        null    | "."
        rootDir | null
        txtFile | "."
    }

    def "Can calculate absolute path"() {
        expect:
        RelativePathUtils.getAbsoluteFile(base, relativePath) == expected

        where:
        base | relativePath               | expected
        ADir | ""                         | ADir
        ADir | "."                        | ADir
        BDir | ".."                       | ADir
        CDir | "..${sep}.."               | ADir
        CDir | "..${sep}..${sep}d"        | DDir
        CDir | "..${sep}..${sep}d${sep}e" | EDir
    }

    private File getRootDir() {
        tempFolder.root
    }

    private String getRootPath() {
        tempFolder.root.absolutePath
    }

    private File getADir() {
        new File(tempFolder.root, 'a')
    }

    private String getAPath() {
        new File(tempFolder.root, 'a').absolutePath
    }

    private File getBDir() {
        new File(tempFolder.root, 'a/b')
    }

    private String getBPath() {
        new File(tempFolder.root, 'a/b').absolutePath
    }

    private File getCDir() {
        new File(tempFolder.root, 'a/b/c')
    }

    private String getCPath() {
        new File(tempFolder.root, 'a/b/c').absolutePath
    }

    private File getDDir() {
        new File(tempFolder.root, 'a/d')
    }

    private String getDPath() {
        new File(tempFolder.root, 'a/d').absolutePath
    }

    private File getEDir() {
        new File(tempFolder.root, 'a/d/e')
    }

    private String getEPath() {
        new File(tempFolder.root, 'a/d/e').absolutePath
    }

    private File getTxtFile() {
        new File(tempFolder.root, 'a/b/c/file.txt')
    }
    private String getTxtPath() {
        new File(tempFolder.root, 'a/b/c/file.txt').absolutePath
    }

    private String getSep() {
        return File.separator
    }

}
