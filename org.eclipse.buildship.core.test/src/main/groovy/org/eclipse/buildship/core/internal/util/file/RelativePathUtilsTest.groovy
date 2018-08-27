package org.eclipse.buildship.core.internal.util.file

import spock.lang.Specification

import org.eclipse.core.runtime.Path

class RelativePathUtilsTest extends Specification {

    def "Relative path calculation throws exception for illegal arguments"() {
        setup:

        when:
        RelativePathUtils.getRelativePath(basePath, targetPath)

        then:
        thrown expected

        where:
        basePath                     | targetPath                   | expected
        null                         | new Path('.')                | NullPointerException
        new Path('.')                | null                         | NullPointerException
        new Path('.').makeRelative() | new Path('.')                | IllegalArgumentException
        new Path('.')                | new Path('.').makeRelative() | IllegalArgumentException
    }

    def "Can calculate relative path"() {
        setup:
        def basePath = new Path(base).makeAbsolute()
        def targetPath = new Path(target).makeAbsolute()
        def expected = new Path(result)

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
        when:
        RelativePathUtils.getAbsolutePath(basePath, relativePath)

        then:
        thrown expected

        where:
        basePath                     | relativePath                 | expected
        null                         | new Path('.')                | NullPointerException
        new Path('.')                | null                         | NullPointerException
        new Path('.').makeRelative() | new Path('.')                | IllegalArgumentException
        new Path('.')                | new Path('.').makeRelative() | IllegalArgumentException
    }

    def "Can calculate absolute path"() {
        setup:
        def basePath = new Path(base).makeAbsolute()
        def relativePath = new Path(path)
        def expected =  new Path(result).makeAbsolute()

        expect:
        RelativePathUtils.getAbsolutePath(basePath, relativePath) == expected

        where:
        base    | path        | result
        'a'     | ''          | 'a'
        'a'     | '.'         | 'a'
        'a/b'   | '..'        | 'a'
        'a/b/c' | '../..'     | 'a'
        'a/b/c' | '../../d'   | 'a/d'
        'a/b/c' | '../../d/e' | 'a/d/e'
        'a/b/c' | 'd/e'       | 'a/b/c/d/e'
    }

    def "Absolute path calculation fails if relative path points above root"() {
         when:
         RelativePathUtils.getAbsolutePath(new Path(fsRoot.absolutePath), new Path('..'))

         then:
         thrown IllegalArgumentException
    }

    private static File getFsRoot(File current = new File('.').absoluteFile) {
        current.parentFile != null ? getFsRoot(current.parentFile) : current
    }

}
