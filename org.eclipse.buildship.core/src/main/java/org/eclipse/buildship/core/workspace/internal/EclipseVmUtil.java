/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.workspace.internal;

import com.google.common.base.Optional;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;

import java.io.File;

/**
 * Helper class to access the installed VMs in the Eclipse registry.
 */
@SuppressWarnings("restriction")
final class EclipseVmUtil {

    private static final String VM_ID_PREFIX = "org.eclipse.buildship.core.vm.";

    private EclipseVmUtil() {
    }

    /**
     * Finds a Java VM in the Eclipse VM registry or registers a new one if none was available with
     * the selected version.
     * @param name the name of the VM
     * @param location the location of the VM
     *
     * @return the reference of an existing or freshly created VM
     */
    public static IVMInstall findOrRegisterVM(String name, File location) {
        Optional<IVMInstall> vm = findRegisteredVM(name);
        return vm.isPresent() ? vm.get() : registerNewVM(name, location);
    }

    private static Optional<IVMInstall> findRegisteredVM(String name) {
        for (IVMInstallType type : JavaRuntime.getVMInstallTypes()) {
            for (IVMInstall instance : type.getVMInstalls()) {
                String instanceName = instance.getName();
                if (instanceName != null && instanceName.equals(name)) {
                    return Optional.of(instance);
                }
            }
        }
        return Optional.absent();
    }

    private static IVMInstall registerNewVM(String name, File location) {
        // use the 'Standard VM' type to register a new VM
        IVMInstallType installType = JavaRuntime.getVMInstallType(StandardVMType.ID_STANDARD_VM_TYPE);

        // both the id and the name have to be unique for the registration
        String vmId = generateUniqueVMId(installType);

        // instantiate the VM and notify Eclipse about the creation
        IVMInstall vm = installType.createVMInstall(vmId);
        vm.setName(name);
        vm.setInstallLocation(location);
        JavaRuntime.fireVMAdded(vm);

        return vm;
    }

    private static String generateUniqueVMId(IVMInstallType type) {
        // return a unique id for the VM
        int counter = 1;
        String vmId = VM_ID_PREFIX + counter;
        while (type.findVMInstall(vmId) != null) {
            counter++;
            vmId = VM_ID_PREFIX + counter;
        }
        return vmId;
    }

}
