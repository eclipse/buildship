package org.eclipse.buildship.core.workspace.internal

import org.eclipse.jdt.internal.launching.StandardVMType
import org.eclipse.jdt.launching.IVMInstall
import org.eclipse.jdt.launching.IVMInstallType
import org.eclipse.jdt.launching.JavaRuntime
import spock.lang.Specification

@SuppressWarnings("restriction")
class EclipseVmUtilTest extends Specification {

    def "Can find an existing VM"() {
        given:
        def name = firstRegisteredVm().name
        def location = firstRegisteredVm().installLocation
        def initialNumOfRegisteredVms = numberOfRegisteredVms()

        when:
        def vm = EclipseVmUtil.findOrRegisterVM(name, location)

        then:
        vm.installLocation == location
        numberOfRegisteredVms() == initialNumOfRegisteredVms
    }

    @SuppressWarnings("GroovyAccessibility")
    def "Creates new VM if none registered with the same name"() {
        given:
        IVMInstall vm = firstRegisteredVm()
        File vmLocation = vm.installLocation
        def initialNumOfRegisteredVms = numberOfRegisteredVms()

        when:
        vm.VMInstallType.disposeVMInstall(vm.id)

        then:
        numberOfRegisteredVms() == initialNumOfRegisteredVms - 1

        when:
        vm = EclipseVmUtil.findOrRegisterVM('JavaSE-1.7', vmLocation)

        then:
        numberOfRegisteredVms() == initialNumOfRegisteredVms

        and:
        vm.id.startsWith EclipseVmUtil.VM_ID_PREFIX
        vm.name == 'JavaSE-1.7'
        vm.VMInstallType.id == StandardVMType.ID_STANDARD_VM_TYPE
        vm.installLocation == vmLocation
    }

    private static def numberOfRegisteredVms() {
        JavaRuntime.VMInstallTypes.sum { it.VMInstalls.length }
    }

    private static def firstRegisteredVm() {
        for (IVMInstallType type : JavaRuntime.VMInstallTypes) {
            for (IVMInstall vm : type.VMInstalls) {
                if (vm.installLocation) {
                    return vm
                }
            }
        }
    }

}

