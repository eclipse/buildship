package org.eclipse.buildship.core.util.file

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class RelativePathUtilsTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    def "Can calculate relative unix path"() {
        expect:
        RelativePathUtils.getRelativePath(base, target, separator) == expected

        where:
        base     | target   | separator | expected
        '/a'     | '/a'     | '/'       | '.'
        '/a'     | '/a/b'   | '/'       | 'b'
        '/a/b'   | '/a'     | '/'       | '..'
        '/a/b/c' | '/a'     | '/'       | '../..'
        '/a/b'   | '/a/c'   | '/'       | '../c'
        '/a/b/c' | '/a/d'   | '/'       | '../../d'
        '/d/e/f' | '/a/b/c' | '/'       | '../../../a/b/c'
    }

    def "Can calculate windows relative path"() {
        expect:
        RelativePathUtils.getRelativePath(base, target, separator) == expected

        where:
        base          | target        | separator | expected
        'C:\\a'       | 'C:\\a'       | '\\'      | '.'
        'C:\\a'       | 'C:\\a\\b'    | '\\'      | 'b'
        'C:\\a\\b'    | 'C:\\a'       | '\\'      | '..'
        'C:\\a\\b\\c' | 'C:\\a'       | '\\'      | '..\\..'
        'C:\\a\\b'    | 'C:\\a\\c'    | '\\'      | '..\\c'
        'C:\\a\\b\\c' | 'C:\\a\\d'    | '\\'      | '..\\..\\d'
        'C:\\d\\e\\f' | 'C:\\a\\b\\c' | '\\'      | '..\\..\\..\\a\\b\\c'
    }

    def "Relative path calculation ignores whitespaces"() {
        expect:
        RelativePathUtils.getRelativePath(base, target, separator) == expected

        where:
        base       | target     | separator | expected
        ' /a '     | ' /a '     | '/'       | '.'
        ' /a '     | ' /a/b '   | '/'       | 'b'
        ' /a/b '   | ' /a '     | '/'       | '..'
        ' /a/b/c ' | ' /a '     | '/'       | '../..'
        ' /a/b '   | ' /a/c '   | '/'       | '../c'
        ' /a/b/c ' | ' /a/d '   | '/'       | '../../d'
        ' /d/e/f ' | ' /a/b/c ' | '/'       | '../../../a/b/c'
    }

    def "Relative path calculation ignores trailing path separators"() {
        expect:
        RelativePathUtils.getRelativePath(base, target, separator) == expected

        where:
        base      | target   | separator | expected
        '/a/'     | '/a/'    | '/'       | '.'
        '/a/'     | '/a/b/'  | '/'       | 'b'
        '/a/b/'   | '/a/'    | '/'       | '..'
        '/a/b/c/' | '/a/'    | '/'       | '../..'
        '/a/b/'   | '/a/c/'  | '/'       | '../c'
        '/a/b/c/' | '/a/d/'  | '/'       | '../../d'
        '/d/e/f/' | '/a/b/c' | '/'       | '../../../a/b/c'
    }

    def "Can calculate absolute unix path"() {
        expect:
        RelativePathUtils.getAbsolutePath(base, target, separator) == expected

        where:
        base     | target    | separator | expected
        '/a'     | ''        | '/'       | '/a'
        '/a'     | '.'       | '/'       | '/a'
        '/a/b'   | '..'      | '/'       | '/a'
        '/a/b/c' | '../..'   | '/'       | '/a'
        '/a/b'   | '../..'   | '/'       | '/'
        '/a/b/c' | '../../d' | '/'       | '/a/d'
        '/a/b'   | '../../d' | '/'       | '/d'
    }

    def "Can calculate absolute windows path"() {
        expect:
        RelativePathUtils.getAbsolutePath(base, target, separator) == expected

        where:
        base          | target      | separator | expected
        'C:\\a'       | ''          | '\\'      | 'C:\\a'
        'C:\\a'       | '.'         | '\\'      | 'C:\\a'
        'C:\\a\\b'    | '..'        | '\\'      | 'C:\\a'
        'C:\\a\\b\\c' | '..\\..'    | '\\'      | 'C:\\a'
        'C:\\a\\b'    | '..\\..'    | '\\'      | 'C:'
        'C:\\a\\b\\c' | '..\\..\\d' | '\\'      | 'C:\\a\\d'
        'C:\\a\\b'    | '..\\..\\d' | '\\'      | 'C:\\d'
    }

    def "Absolute path calculation ignores whitespaces"() {
        expect:
        RelativePathUtils.getAbsolutePath(base, target, separator) == expected

        where:
        base       | target      | separator | expected
        ' /a '     | ' '         | '/'       | '/a'
        ' /a '     | ' . '       | '/'       | '/a'
        ' /a/b '   | ' .. '      | '/'       | '/a'
        ' /a/b/c ' | ' ../.. '   | '/'       | '/a'
        ' /a/b '   | ' ../.. '   | '/'       | '/'
        ' /a/b/c ' | ' ../../d ' | '/'       | '/a/d'
        ' /a/b '   | ' ../../d ' | '/'       | '/d'
    }

    def "Absolute path calculation ignores trailing path separators"() {
        expect:
        RelativePathUtils.getAbsolutePath(base, target, separator) == expected

        where:
        base      | target     | separator | expected
        '/a/'     | ''         | '/'       | '/a'
        '/a/'     | './'       | '/'       | '/a'
        '/a/b/'   | '../'      | '/'       | '/a'
        '/a/b/c/' | '../../'   | '/'       | '/a'
        '/a/b/'   | '../../'   | '/'       | '/'
        '/a/b/c/' | '../../d/' | '/'       | '/a/d'
        '/a/b/'   | '../../d/' | '/'       | '/d'
    }

    def "Absolute path calculation throws exception when relative path points beyond root"() {
        when:
        RelativePathUtils.getAbsolutePath('/a', '../..', '/')

        then:
        thrown IllegalArgumentException
    }

    def "Can calculate relative path from directory"() {
        setup:
        def base = tempFolder.newFolder('a', 'b')
        def target = new File(tempFolder.root, 'a')

        expect:
        RelativePathUtils.getRelativePath(base, target) == '..'
    }

    def "Can calculate relative path from file"() {
        setup:
        def testFolder = tempFolder.newFolder('a', 'b')
        def base = tempFolder.newFile('a/b/c.txt')
        def target = new File(tempFolder.root, 'a')

        expect:
        RelativePathUtils.getRelativePath(base, target) == '..'
    }

    def "Can calculate absolute file from directory"() {
        setup:
        def base = tempFolder.newFolder('a', 'b')
        def target = new File(tempFolder.root, 'a')

        expect:
        RelativePathUtils.getAbsoluteFile(base, '..') == new File(tempFolder.root, 'a')
    }

    def "Can calculate absolute file from file"() {
        setup:
        def testFolder = tempFolder.newFolder('a', 'b')
        def base = tempFolder.newFile('a/b/c.txt')

        expect:
        RelativePathUtils.getAbsoluteFile(base, '..') == new File(tempFolder.root, 'a')
    }

}
