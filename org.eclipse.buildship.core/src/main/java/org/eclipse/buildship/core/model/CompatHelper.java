package org.eclipse.buildship.core.model;

import java.util.Collections;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.internal.ImmutableDomainObjectSet;

final class CompatHelper {


    private CompatHelper() {
    }

    static <T> DomainObjectSet<? extends T> asDomainSet(Iterable<? extends T> result) {
        return ImmutableDomainObjectSet.of(result);
    }

    static <T> DomainObjectSet<? extends T> emptyDomainSet() {
        return ImmutableDomainObjectSet.of(Collections.<T> emptyList());
    }
}
