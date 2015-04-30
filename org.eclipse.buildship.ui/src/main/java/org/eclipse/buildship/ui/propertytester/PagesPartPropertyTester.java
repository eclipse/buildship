package org.eclipse.buildship.ui.propertytester;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;

import org.eclipse.buildship.ui.part.execution.AbstractPagePart;
import org.eclipse.buildship.ui.part.pages.IPage;

public class PagesPartPropertyTester extends PropertyTester {

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
