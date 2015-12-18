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

import java.io.File;

import com.google.common.base.Optional;

import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Helper class to access the installed VMs in the Eclipse registry.
 */
@SuppressWarnings("restriction")
final class EclipseVmUtil {

    private static final String VM_ID_PREFIX = "org.eclipse.buildship.core.vm.";
    private static final String VM_NAME_PREFIX = "Java SE";

    private EclipseVmUtil() {
    }

    /**
     * Finds a Java VM in the Eclipse VM registry or registers a new one if none was available with
     * the target install location.
     *
     * @param installLocation the location of the VM
     * @param version the Java version of the VM, used only to generate a proper display name
     * @return the reference of an existing or freshly created VM
     */
    public static IVMInstall findOrRegisterVM(File installLocation, String version) {
        Optional<IVMInstall> vm = findRegisteredVM(installLocation);
        if (vm.isPresent()) {
            return vm.get();
        } else {
            return registerNewVM(installLocation, version);
        }
    }

    private static Optional<IVMInstall> findRegisteredVM(File installLocation) {
        for (IVMInstallType type : JavaRuntime.getVMInstallTypes()) {
            for (IVMInstall instance : type.getVMInstalls()) {
                File location = instance.getInstallLocation();
                if (location != null && location.equals(installLocation)) {
                    return Optional.of(instance);
                }
            }
        }
        return Optional.absent();
    }

    private static IVMInstall registerNewVM(File installLocation, String version) {
        // use the 'Standard VM' type to register a new VM
        IVMInstallType installType = JavaRuntime.getVMInstallType(StandardVMType.ID_STANDARD_VM_TYPE);

        // both the id and the name has to be unique for the registration
        String vmId = generateUniqueVMId(installType);
        String vmName = generateUniqueVMName(version, installType);

        // instantiate the VM and notify Eclipse about the creation
        IVMInstall vm = installType.createVMInstall(vmId);
        vm.setName(vmName);
        vm.setInstallLocation(installLocation);
        JavaRuntime.fireVMAdded(vm);

        return vm;
    }

    private static String generateUniqueVMId(IVMInstallType installType) {
        // return a unique id for the VM
        int counter = 1;
        String vmId = VM_ID_PREFIX + counter;
        while (installType.findVMInstall(vmId) != null) {
            counter++;
            vmId = VM_ID_PREFIX + counter;
        }
        return vmId;
    }

    private static String generateUniqueVMName(String version, IVMInstallType installType) {
        // for a Java 1.7 JVM return the name 'Java SE 7' if not taken otherwise
        // return the first non taken value from 'Java SE 7 (2)', 'Java SE 7 (3)', etc.
        int counter = 1;
        String vmName = VM_NAME_PREFIX + " " + version;
        while (installType.findVMInstallByName(vmName) != null) {
            counter++;
            vmName = VM_NAME_PREFIX + " " + version + " (" + counter + ")";
        }
        return vmName;
    }

}
