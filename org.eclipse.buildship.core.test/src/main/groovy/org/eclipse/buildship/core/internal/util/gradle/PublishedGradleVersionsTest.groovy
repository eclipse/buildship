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
            '2.3-rc-1',
            '2.2.1',
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
    "version": "2.3-rc-1",
    "buildTime": "20150127140232+0000",
    "current": false,
    "snapshot": false,
    "nightly": false,
    "activeRc": true,
    "rcFor": "2.3",
    "broken": false,
    "downloadUrl": "https:\\/\\/services.gradle.org\\/distributions\\/gradle-2.3-rc-1-bin.zip"
  },
  {
    "version": "2.2.1",
    "buildTime": "20141124094535+0000",
    "current": true,
    "snapshot": false,
    "nightly": false,
    "activeRc": false,
    "rcFor": "",
    "broken": false,
    "downloadUrl": "https:\\/\\/services.gradle.org\\/distributions\\/gradle-2.2.1-bin.zip"
  }
]
        '''
    }
}
