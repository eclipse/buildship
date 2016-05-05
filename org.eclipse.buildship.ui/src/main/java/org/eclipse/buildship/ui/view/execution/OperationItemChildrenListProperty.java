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

package org.eclipse.buildship.ui.view.execution;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.property.list.DelegatingListProperty;
import org.eclipse.core.databinding.property.list.IListProperty;

/**
 * This class is used for the data binding of the {@code OperationItem} nodes in a tree. It specifies
 * that the {@link org.eclipse.buildship.ui.view.execution.OperationItem#FIELD_CHILDREN} are the children
 * <p/>
 * In case there are other types of nodes in a tree with different properties for their children, they can
 * be listed here.
 */
@SuppressWarnings("rawtypes")
public final class OperationItemChildrenListProperty extends DelegatingListProperty {

    @Override
    protected IListProperty doGetDelegate(Object source) {
        if (source instanceof OperationItem) {
            return BeanProperties.list(OperationItem.class, OperationItem.FIELD_CHILDREN);
        } else {
            return null;
        }
    }

}
