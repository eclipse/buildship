/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.test.fixtures;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;

/**
 * A specification that matches against Gradle version patterns.
 *
 * @author Etienne Studer
 */
public abstract class GradleVersionConstraints {

    private static final String CURRENT = "current";
    private static final String NOT_CURRENT = "!current";
    private static final String EQUALS = "=";
    private static final String NOT_EQUALS = "!=";
    private static final String GREATER_THAN_OR_EQUALS = ">=";
    private static final String GREATER_THAN = ">";
    private static final String SMALLER_THAN_OR_EQUALS = "<=";
    private static final String SMALLER_THAN = "<";

    private GradleVersionConstraints() {
    }

    /**
     * Creates a predicate from the given version constraint.
     *
     * @param constraint the version constraint, must not be null
     * @return the predicate representing the version constraint, never null
     */
    public static Predicate<GradleVersion> toPredicate(String constraint) {
        Preconditions.checkNotNull(constraint);

        String trimmed = constraint.trim();

        // exclusive patterns
        if (trimmed.equals(CURRENT)) {
            final GradleVersion current = GradleVersion.current();
            return new Predicate<GradleVersion>() {

                @Override
                public boolean apply(GradleVersion element) {
                    return element.equals(current);
                }
            };
        }
        if (trimmed.equals(NOT_CURRENT)) {
            final GradleVersion current = GradleVersion.current();
            return new Predicate<GradleVersion>() {

                @Override
                public boolean apply(GradleVersion element) {
                    return !element.equals(current);
                }
            };
        }
        if (trimmed.startsWith(EQUALS)) {
            final GradleVersion target = GradleVersion.version(trimmed.substring(1)).getBaseVersion();
            return new Predicate<GradleVersion>() {

                @Override
                public boolean apply(GradleVersion element) {
                    return element.getBaseVersion().equals(target);
                }
            };
        }

        // AND-combined patterns
        final List<Predicate<GradleVersion>> predicates = new ArrayList<Predicate<GradleVersion>>();
        String[] patterns = trimmed.split("\\s+");
        for (String value : patterns) {
            if (value.startsWith(NOT_EQUALS)) {
                final GradleVersion version = GradleVersion.version(value.substring(2));
                predicates.add(new Predicate<GradleVersion>() {

                    @Override
                    public boolean apply(GradleVersion element) {
                        return !element.getBaseVersion().equals(version);
                    }
                });
            } else if (value.startsWith(GREATER_THAN_OR_EQUALS)) {
                final GradleVersion minVersion = GradleVersion.version(value.substring(2));
                predicates.add(new Predicate<GradleVersion>() {

                    @Override
                    public boolean apply(GradleVersion element) {
                        return element.getBaseVersion().compareTo(minVersion) >= 0;
                    }
                });
            } else if (value.startsWith(GREATER_THAN)) {
                final GradleVersion minVersion = GradleVersion.version(value.substring(1));
                predicates.add(new Predicate<GradleVersion>() {

                    @Override
                    public boolean apply(GradleVersion element) {
                        return element.getBaseVersion().compareTo(minVersion) > 0;
                    }
                });
            } else if (value.startsWith(SMALLER_THAN_OR_EQUALS)) {
                final GradleVersion maxVersion = GradleVersion.version(value.substring(2));
                predicates.add(new Predicate<GradleVersion>() {

                    @Override
                    public boolean apply(GradleVersion element) {
                        return element.getBaseVersion().compareTo(maxVersion) <= 0;
                    }
                });
            } else if (value.startsWith(SMALLER_THAN)) {
                final GradleVersion maxVersion = GradleVersion.version(value.substring(1));
                predicates.add(new Predicate<GradleVersion>() {

                    @Override
                    public boolean apply(GradleVersion element) {
                        return element.getBaseVersion().compareTo(maxVersion) < 0;
                    }
                });
            } else {
                throw new RuntimeException(String
                        .format("Unsupported version range '%s' specified in constraint '%s'. Supported formats: '>=nnn' or '<=nnn' or space-separate patterns", value, constraint));
            }
        }

        return new Predicate<GradleVersion>() {

            @Override
            public boolean apply(GradleVersion element) {
                for (Predicate<GradleVersion> predicate : predicates) {
                    if (!predicate.apply(element)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

}
