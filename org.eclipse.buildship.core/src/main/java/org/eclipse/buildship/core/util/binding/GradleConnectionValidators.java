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

package org.eclipse.buildship.core.util.binding;

import java.io.File;

import com.google.common.base.Optional;

import com.gradleware.tooling.toolingutils.binding.Validator;

import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.core.i18n.CoreMessages;

/**
 * Factory class for {@link Validator} instances that validate Gradle connections attributes.
 */
public final class GradleConnectionValidators {

    private GradleConnectionValidators() {
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

    public static <T> Validator<T> nullValidator() {
        return new Validator<T>() {

            @Override
            public Optional<String> validate(T value) {
                return Optional.absent();
            }

        };
    }

}
