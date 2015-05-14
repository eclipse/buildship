/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.propertytester;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;

import org.eclipse.buildship.ui.part.AbstractPagePart;
import org.eclipse.buildship.ui.part.pages.IPage;

/**
 * PropertyTester for an {@link AbstractPagePart}. See {@link Properties} for properties, which can
 * be tested.
 *
 */
public class PagesPartPropertyTester extends PropertyTester {

    /**
     * Properties, which can be checked by this PropertyTester.
     */
    private enum Properties {
        hasPages
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

        Properties propertyValue = Properties.valueOf(property);

        if (Properties.hasPages.equals(propertyValue)) {
            if (receiver instanceof AbstractPagePart) {
                AbstractPagePart pagePart = (AbstractPagePart) receiver;
                List<IPage> pages = pagePart.getPages();
                return pages.size() > 0;
            }
        }

        return false;
    }

}
