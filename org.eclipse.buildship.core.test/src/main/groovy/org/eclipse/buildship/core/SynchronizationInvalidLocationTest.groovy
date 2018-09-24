package org.eclipse.buildship.core

import org.eclipse.core.runtime.IStatus

import org.eclipse.buildship.core.internal.operation.ToolingApiStatus.ToolingApiStatusType
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class SynchronizationInvalidLocationTest extends ProjectSynchronizationSpecification {

   def "Can import a nonexistent location"() {
       setup:
       File location = new File('nonexistent')

       when:
       SynchronizationResult result = tryImportAndWait(location)

       then:
       result.status.isOK()
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
