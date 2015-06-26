/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 465728
 */

package org.eclipse.buildship.ui.templates;

import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * {@link TemplateContextType} for gradle.
 *
 */
public class GradleTemplateContextType extends TemplateContextType {

    public GradleTemplateContextType() {
        super("gradle", "Gradle");
    }

}
