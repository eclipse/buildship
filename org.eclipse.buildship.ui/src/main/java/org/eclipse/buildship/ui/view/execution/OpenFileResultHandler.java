/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */

package org.eclipse.buildship.ui.view.execution;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.IRegion;

/**
 * Internal contract how to open files in {@link OpenTestSourceFileAction}.
 */
interface OpenFileResultHandler {

    void openGenericSource(IFile file, IRegion region);

    void openJavaSource(IJavaElement javaElement);
}
