/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.gradle

import spock.lang.Shared;
import spock.lang.Specification

import org.eclipse.buildship.core.internal.util.gradle.PublishedGradleVersions.LookupStrategy

class PublishedGradleVersionsTest extends Specification {

    @Shared File CACHE_FILE = PublishedGradleVersions.cacheFile

    def setup() {
        CACHE_FILE.delete()
    }

    def "Can parse valid version info file"() {
        setup:
        PublishedGradleVersions publishedVersions = PublishedGradleVersions.create(publishedGradleVersionJson)
        assert publishedVersions.versions == [
            '4.4-rc-6',
            '4.4',
        ].collect { GradleVersion.version(it) }
    }

    def "When REMOTE strategy is specified, no cache is written"() {
        when:
        PublishedGradleVersions publishedVersions = PublishedGradleVersions.create(LookupStrategy.REMOTE)
        then:
        publishedVersions.versions.size() >= 18
        !CACHE_FILE.exists()
    }

    def "When REMOTE_IF_NOT_CACHED strategy is specified, a cache file is written"() {
        when:
        PublishedGradleVersions publishedVersions = PublishedGradleVersions.create(LookupStrategy.REMOTE_IF_NOT_CACHED)
        then:
        publishedVersions.versions.size() >= 18
        CACHE_FILE.exists()
    }

    def "The cache file is used if availabe and not outdated"() {
        setup:
        PublishedGradleVersions.create(LookupStrategy.REMOTE_IF_NOT_CACHED)
        def lastModified = CACHE_FILE.lastModified()
        Thread.sleep(500)

        when:
        def publishedVersions = PublishedGradleVersions.create(LookupStrategy.REMOTE_IF_NOT_CACHED)
        def lastModifiedAfterCacheHit = CACHE_FILE.lastModified()

        then:
        publishedVersions.versions.size() >= 18
        assert lastModifiedAfterCacheHit == lastModified
    }

    def "No remote download is attempted if CACHE_ONLY is specified"() {
        when:
        PublishedGradleVersions publishedVersions = PublishedGradleVersions.create(LookupStrategy.CACHED_ONLY)
        then:
        thrown IllegalStateException
    }

    private String getPublishedGradleVersionJson() {
        '''
[
  {
    "version" : "4.4-rc-6",
    "buildTime" : "20171204084815+0000",
    "current" : false,
    "snapshot" : false,
    "nightly" : false,
    "releaseNightly" : false,
    "activeRc" : true,
    "rcFor" : "4.4",
    "milestoneFor" : "",
    "broken" : false,
    "downloadUrl" : "https://services.gradle.org/distributions/gradle-4.4-rc-6-bin.zip",
    "checksumUrl" : "https://services.gradle.org/distributions/gradle-4.4-rc-6-bin.zip.sha256"
  },
  {
    "version" : "4.4",
    "buildTime" : "20171206090506+0000",
    "current" : false,
    "snapshot" : false,
    "nightly" : false,
    "releaseNightly" : false,
    "activeRc" : false,
    "rcFor" : "",
    "milestoneFor" : "",
    "broken" : false,
    "downloadUrl" : "https://services.gradle.org/distributions/gradle-4.4-bin.zip",
    "checksumUrl" : "https://services.gradle.org/distributions/gradle-4.4-bin.zip.sha256"
  }
]
        '''
    }
}
