/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core

import org.eclipse.core.runtime.IStatus

import org.eclipse.buildship.core.internal.operation.ToolingApiStatus.ToolingApiStatusType
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class SynchronizationInvalidLocationTest extends ProjectSynchronizationSpecification {

   def "Cannot import a nonexistent location"() {
       setup:
       File location = new File('nonexistent')

       when:
       SynchronizationResult result = tryImportAndWait(location)

       then:
       result.status.severity == IStatus.WARNING
       ToolingApiStatusType.IMPORT_ROOT_DIR_FAILED.matches(result.status)
   }

   def "Cannot import a plain file"() {
       setup:
       File location = file('nonexistent.file')

       when:
       SynchronizationResult result = tryImportAndWait(location)

       then:
       result.status.severity == IStatus.WARNING
       ToolingApiStatusType.IMPORT_ROOT_DIR_FAILED.matches(result.status)
   }
}
