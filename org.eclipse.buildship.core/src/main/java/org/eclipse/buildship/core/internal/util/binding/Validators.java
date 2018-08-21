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

package org.eclipse.buildship.core.internal.util.binding;

import java.io.File;
import java.util.Arrays;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.core.internal.i18n.CoreMessages;

/**
 * Factory class for common {@link Validator} instances.
 */
public final class Validators {

    private static final Validator<Object> NO_OP = new Validator<Object>() {
        @Override
        public Optional<String> validate(Object value) {
            return Optional.absent();
        }
    };

    private Validators() {
    }

    public static <T> Validator<T> noOp() {
        @SuppressWarnings("unchecked")
        Validator<T> noOp = (Validator<T>) NO_OP;
        return noOp;
    }


    public static Validator<File> requiredDirectoryValidator(final String prefix) {
        return new Validator<File>() {

            @Override
            public Optional<String> validate(File file) {
                if (file == null) {
                    return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, prefix));
                } else if (!file.exists()) {
                    return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_DoesNotExist, prefix));
                } else if (!file.isDirectory()) {
                    return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeDirectory, prefix));
                } else {
                    return Optional.absent();
                }
            }
        };
    }

    public static Validator<File> optionalDirectoryValidator(final String prefix) {
        return new Validator<File>() {

            @Override
            public Optional<String> validate(File file) {
                if (file == null) {
                    return Optional.absent();
                } else if (!file.exists()) {
                    return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_DoesNotExist, prefix));
                } else if (!file.isDirectory()) {
                    return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeDirectory, prefix));
                } else {
                    return Optional.absent();
                }
            }
        };
    }

    public static Validator<File> nonExistentDirectoryValidator(final String prefix) {
        return new Validator<File>() {

            @Override
            public Optional<String> validate(File file) {
                if (file == null) {
                    return Optional.absent();
                } else if (file.exists()) {
                    return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_AlreadyExists, prefix));
                } else {
                    return Optional.absent();
                }
            }
        };
    }

    public static Validator<File> nonWorkspaceFolderValidator(final String prefix) {
        final File workspaceDir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
        return new Validator<File>() {

            @Override
            public Optional<String> validate(File file) {
                if (workspaceDir.equals(file)) {
                    return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_WorkspaceDirectory, prefix));
                } else {
                    return Optional.absent();
                }
            }
        };
    }

    public static Validator<String> uniqueWorkspaceProjectNameValidator(final String prefix) {
        return new Validator<String>() {

            @Override
            public Optional<String> validate(String projectName) {
                if (Strings.isNullOrEmpty(projectName)) {
                    return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_MustBeSpecified, prefix));
                } else if (projectNameAlreadyExistsInWorkspace(projectName)) {
                    return Optional.of(NLS.bind(CoreMessages.ErrorMessage_0_AlreadyExists, prefix));
                } else {
                    return Optional.absent();
                }
            }

            private boolean projectNameAlreadyExistsInWorkspace(final String projectName) {
                IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
                return FluentIterable.from(Arrays.asList(projects)).anyMatch(new Predicate<IProject>() {

                    @Override
                    public boolean apply(IProject project) {
                        return projectName.equals(project.getName());
                    }
                });
            }
        };
    }

    public static <T> Validator<T> validateIfConditionFalse(final Validator<T> validator, final Property<Boolean> condition) {
        return new Validator<T>() {
            @Override
            public Optional<String> validate(T value) {
                return Boolean.FALSE.equals(condition.getValue()) ? validator.validate(value) : Optional.<String>absent();
            }
        };
    }

    public static <T> Validator<T> and(final Validator<T> a, final Validator<T> b) {
        return new Validator<T>() {
            @Override
            public Optional<String> validate(T value) {
                Optional<String> result = a.validate(value);
                return result.isPresent() ? result : b.validate(value);
            }
        };
    }

    public static <T> Validator<T> nullValidator() {
        return new Validator<T>() {

            @Override
            public Optional<String> validate(T value) {
                return Optional.absent();
            }

        };
    }

}
