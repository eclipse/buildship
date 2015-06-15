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

/**
 * OSGi-specific version.
 */
final class Version {
  long major = 0, minor = 0, release = 0
  String suffix = ''

  Version(eclipsebuild.mavenize.Pom pom) {
    init(pom.version)
  }

  Version(String versionStr) {
    init(versionStr)
  }

  int compare(Version other) {
    int result = major - other.major
    if(result != 0)
      return result
    result = minor - other.minor
    if(result != 0)
      return result
    result = release - other.release
    if(result != 0)
      return result
    return suffix.compareTo(other.suffix)
  }

  private void init(String versionStr) {
    def m = versionStr =~ /(\d+)(\.(\d+))?(\.(\d+))?(\.(.+))?/
    if(m) {
      major = Long.valueOf(m[0][1] ?: '0')
      minor = Long.valueOf(m[0][3] ?: '0')
      release = Long.valueOf(m[0][5] ?: '0')
      suffix = m[0][7] ?: ''
    }
  }
}
