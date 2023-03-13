/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.string

import java.util.regex.Matcher
import java.util.regex.Pattern
import spock.lang.Specification

class PatternUtilsTest extends Specification {

    def "Matching web urls"(String string) {
        Pattern pattern = Pattern.compile(PatternUtils.WEB_URL_PATTERN)
        Matcher matcher = pattern.matcher(string)

        expect:
        matcher.matches()
        matcher.group(0) == string

        where:
        string | _
        'http://asdf.net' | _
        'http://foo.com/blah_blah' | _
        'http://foo.com/blah_blah/' | _
        'http://www.example.com/wpstyle/?p=364' | _
        'https://www.example.com/foo/?bar=baz&inga=42&quux' | _
        'http://✪df.ws/123' | _
        'http://userid:password@example.com:8080' | _
        'http://userid:password@example.com:8080/' | _
        'http://userid@example.com' | _
        'http://userid@example.com/' | _
        'http://userid@example.com:8080' | _
        'http://userid@example.com:8080/' | _
        'http://userid:password@example.com' | _
        'http://userid:password@example.com/' | _
        'http://142.42.1.1/' | _
        'http://142.42.1.1:8080/' | _
        'http://➡.ws/䨹' | _
        'http://⌘.ws' | _
        'http://⌘.ws/' | _
        'http://foo.com/blah_(wikipedia)#cite-1' | _
        'http://foo.com/blah_(wikipedia)_blah#cite-1' | _
        'http://foo.com/unicode_(✪)_in_parens' | _
        'http://foo.com/(something)?after=parens' | _
        'http://☺.damowmow.com/' | _
        'http://code.google.com/events/#&product=browser' | _
        'http://j.mp' | _
        'http://foo.bar/?q=Test%20URL-encoded%20stuff' | _
        'http://مثال.إختبار' | _
        'http://例子.测试' | _
        'http://1337.net' | _
        'http://a.b-c.de' | _
        'http://223.255.255.254' | _
    }

    def "Doesn't match on strings without protocol"(String string) {
        Pattern pattern = Pattern.compile(PatternUtils.WEB_URL_PATTERN)
        Matcher matcher = pattern.matcher(string)

        expect:
        !matcher.matches()

        where:
        string | _
        '65.jdk/Contents/Home' | _
        'org.eclipse.buildship.core' | _
    }
}
