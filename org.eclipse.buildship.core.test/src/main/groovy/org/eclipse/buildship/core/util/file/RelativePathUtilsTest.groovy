package org.eclipse.buildship.core.util.file

import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import org.eclipse.core.runtime.Path

class RelativePathUtilsTest extends Specification {

    def "Relative path calculation throws exception for illegal arguments"() {
        setup:
        def basePath = base == null ? null : new Path(base)
        def targetPath = target == null ? null : new Path(target)

        when:
        RelativePathUtils.getRelativePath(basePath, targetPath)

        then:
        thrown NullPointerException

        where:
        base | target
        null | '.'
        '.'  | null
    }

    def "Can calculate relative path"() {
        setup:
        def basePath = new Path(base)
        def targetPath = new Path(target)
        def expected = result.replace('/', File.separator)

        expect:
        RelativePathUtils.getRelativePath(basePath, targetPath) == expected

        where:
        base         | target  | result
        'a'          | 'a'     | ''
        'a'          | 'a/b'   | 'b'
        'a/b'        | 'a'     | '..'
        'a/b/c'      | 'a'     | '../..'
        'a/d'        | 'a/b'   | '../b'
        'a/d/e'      | 'a/b'   | '../../b'
        'a/d/e'      | 'a/b/c' | '../../b/c'
        'a/b/../b/c' | 'a/b'   | '..'
    }

    def "Absolute path calculation throws exception for illegal arguments"() {
        setup:
        def basePath = base == null ? null : new Path(base)

        when:
        RelativePathUtils.getAbsolutePath(basePath, relativePath)

        then:
        thrown NullPointerException

        where:
        base | relativePath
        null | '.'
        '.'  | null
    }

    def "Can calculate absolute path"() {
        setup:
        def baseFile = new Path(base)
        def relativePath = path.replace('/', File.separator)
        def expected =  new Path(result).makeAbsolute()

        expect:
        RelativePathUtils.getAbsolutePath(baseFile, relativePath) == expected

        where:
        base    | path        | result
        'a'     | ''          | 'a'
        'a'     | '.'         | 'a'
        'a/b'   | '..'        | 'a'
        'a/b/c' | '../..'     | 'a'
        'a/b/c' | '../../d'   | 'a/d'
        'a/b/c' | '../../d/e' | 'a/d/e'
    }

    def "Absolute path calculation fails if relative path points above root"() {
         when:
         RelativePathUtils.getAbsolutePath(new Path(fsRoot.absolutePath), '..')

         then:
         thrown IllegalArgumentException
    }

    private static File getFsRoot(File current = new File('.').absoluteFile) {
        current.parentFile != null ? getFsRoot(current.parentFile) : current
    }

}
