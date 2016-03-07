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
import java.util.Arrays;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

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

    private EclipseVmUtil() {
    }

    /**
     * Finds a Java VM in the Eclipse VM registry or registers a new one if none was available with
     * the selected version.
     *
     * @param version the VM's supported Java version
     * @param location the location of the VM
     * @return the reference of an existing or freshly created VM
     */
    public static IVMInstall findOrRegisterStandardVM(String version, File location) {
        Preconditions.checkNotNull(version);
        Preconditions.checkNotNull(location);

        String eeName = resolveEeName(version);
        return findOrRegisterVM(eeName, location);
    }

    private static IVMInstall findOrRegisterVM(String name, File location) {
        Optional<IVMInstall> vm = findRegisteredVM(name);
        return vm.isPresent() ? vm.get() : registerNewVM(name, location);
    }

    private static String resolveEeName(String version) {
        // the result values correspond to the standard execution environment definitions in the
        // org.eclipse.jdt.launching/plugin.xml file
        if ("1.1".equals(version)) {
            return "JRE-1.1";
        } else if (Arrays.asList("1.5", "1.4", "1.3", "1.2").contains(version)) {
            return "J2SE-" + version;
        } else {
            return "JavaSE-" + version;
        }
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
