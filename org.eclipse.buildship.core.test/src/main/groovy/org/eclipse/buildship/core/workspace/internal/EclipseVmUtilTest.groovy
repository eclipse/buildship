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
        vm = EclipseVmUtil.findOrRegisterVM('a-new-vm', vmLocation)

        then:
        numberOfRegisteredVms() == initialNumOfRegisteredVms

        and:
        vm.id.startsWith EclipseVmUtil.VM_ID_PREFIX
        vm.name == 'a-new-vm'
        vm.VMInstallType.id == StandardVMType.ID_STANDARD_VM_TYPE
        vm.installLocation == vmLocation
    }

    def "New VMs names correspond to execution environment names"() {
        expect:
        EclipseVmUtil.resolveEeName(version) == expected

        where:
        version | expected
        '1.1'   | 'JRE-1.1'
        '1.2'   | 'J2SE-1.2'
        '1.3'   | 'J2SE-1.3'
        '1.4'   | 'J2SE-1.4'
        '1.5'   | 'J2SE-1.5'
        '1.6'   | 'JavaSE-1.6'
        '1.7'   | 'JavaSE-1.7'
        '1.8'   | 'JavaSE-1.8'
        '1.9'   | 'JavaSE-1.9'
    }

    def "Unrecognized versions get sensible vm names"() {
        expect:
        EclipseVmUtil.resolveEeName(version) == expected

        where:
        version   | expected
        '1.0'     | 'JavaSE-1.0'
        'unknown' | 'JavaSE-unknown'
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

