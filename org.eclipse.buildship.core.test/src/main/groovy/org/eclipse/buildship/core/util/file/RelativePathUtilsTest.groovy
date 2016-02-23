package org.eclipse.buildship.core.util.file

import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

class RelativePathUtilsTest extends Specification {

    @ClassRule @Shared TemporaryFolder tempFolder

    // root
    // |- a
    //    |-b/c
    //    |-d/e

    def setupSpec() {
        tempFolder.newFolder('a', 'b', 'c')
        tempFolder.newFolder('a', 'd', 'e')
    }

    def "Relative path calculation throws exception for illegal arguments"() {
        when:
        RelativePathUtils.getRelativePath(base, target) == '..'

        then:
        thrown NullPointerException

        where:
        base            | target
        null            | tempFolder.root
        tempFolder.root | null
    }

    def "Can calculate relative path"() {
        setup:
        def baseFile = new File(tempFolder.root, base)
        def targetFile = new File(tempFolder.root, target)
        def expected = result.replace('/', File.separator)

        expect:
        RelativePathUtils.getRelativePath(baseFile, targetFile) == expected

        where:
        base         | target  | result
        'a'          | 'a'     | "."
        'a'          | 'a/b'   | "b"
        'a/b'        | 'a'     | ".."
        'a/b/c'      | 'a'     | "../.."
        'a/d'        | 'a/b'   | "../b"
        'a/d/e'      | 'a/b'   | "../../b"
        'a/d/e'      | 'a/b/c' | "../../b/c"
        'a/b/../b/c' | 'a/b'   | '..'
    }

    def "Absolute path calculation throws exception for illegal arguments"() {
        when:
        RelativePathUtils.getAbsoluteFile(base, path)

        then:
        thrown NullPointerException

        where:
        base            | path
        null            | "."
        tempFolder.root | null
    }

    def "Can calculate absolute path"() {
        setup:
        def baseFile = new File(tempFolder.root, base)
        def relativePath = path.replace('/', File.separator)
        def expected =  new File(tempFolder.root, result)

        expect:
        RelativePathUtils.getAbsoluteFile(baseFile, relativePath) == expected

        where:
        base    | path        | result
        'a'     | ""          | 'a'
        'a'     | "."         | 'a'
        'a/b'   | ".."        | 'a'
        'a/b/c' | "../.."     | 'a'
        'a/b/c' | "../../d"   | 'a/d'
        'a/b/c' | "../../d/e" | 'a/d/e'
    }

}
