package org.eclipse.buildship.core.workspace.internal

import java.io.File

import spock.lang.Specification

import org.eclipse.jdt.internal.launching.StandardVMType
import org.eclipse.jdt.launching.IVMInstall
import org.eclipse.jdt.launching.IVMInstallType
import org.eclipse.jdt.launching.JavaRuntime

@SuppressWarnings("restriction")
class EclipseVmUtilTest extends Specification {

    def "Can find an existing VM"() {
        given:
        def initialNumOfRegisteredVms = numberOfRegisteredVms()
        def location = firstRegisteredVm().installLocation

        when:
        def vm = EclipseVmUtil.findOrRegisterVM(location, '1.7')
        def currentNumOfRegisteredVms = numberOfRegisteredVms()

        then:
        vm.installLocation == location
        initialNumOfRegisteredVms == currentNumOfRegisteredVms
    }

    def "Creates new VM if none registered at the same location"() {
        given:
        IVMInstall vm = firstRegisteredVm()
        File vmLocation = vm.installLocation
        def initialNumOfRegisteredVms = numberOfRegisteredVms()

        when:
        vm.VMInstallType.disposeVMInstall(vm.id)
        def currentNumOfRegisteredVms = numberOfRegisteredVms()

        then:
        currentNumOfRegisteredVms == initialNumOfRegisteredVms - 1

        when:
        vm = EclipseVmUtil.findOrRegisterVM(vmLocation, '1.7')
        currentNumOfRegisteredVms = numberOfRegisteredVms()

        then:
        currentNumOfRegisteredVms == initialNumOfRegisteredVms
        vm.id.startsWith EclipseVmUtil.VM_ID_PREFIX
        vm.name.startsWith EclipseVmUtil.VM_NAME_PREFIX
        vm.name.contains '1.7'
        vm.VMInstallType.id == StandardVMType.ID_STANDARD_VM_TYPE
        vm.installLocation == vmLocation
    }

    private def numberOfRegisteredVms() {
        JavaRuntime.VMInstallTypes.sum { it.VMInstalls.length }
    }

    private def firstRegisteredVm() {
        for (IVMInstallType type : JavaRuntime.VMInstallTypes) {
            for (IVMInstall vm : type.VMInstalls) {
                if (vm.installLocation) {
                    return vm
                }
            }
        }
    }
}

