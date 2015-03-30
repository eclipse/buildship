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

package com.gradleware.tooling.eclipse.core.util.progress;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Simple {@link ISchedulingRule} implementation forcing only one job instance to run at once among
 * all jobs with the associated rule instance applied.
 */
public final class UniqueJobRule implements ISchedulingRule {

    private UniqueJobRule() {
    }

    @Override
    public boolean contains(ISchedulingRule rule) {
        return rule == this;
    }

    @Override
    public boolean isConflicting(ISchedulingRule rule) {
        return rule == this;
    }

    /**
     * Creates a new instance.
     *
     * @return the new instance
     */
    public static ISchedulingRule newInstance() {
        return new UniqueJobRule();
    }

}
