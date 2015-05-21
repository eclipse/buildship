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

package org.eclipse.buildship.ui.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A {@link MultiPageView} page displaying a message.
 */
public final class MessagePage extends BasePage<Composite> {

    private final String message;
    private Label messageLabel;

    /**
     * Creates a new page. The message is the empty string.
     */
    public MessagePage(String message) {
        this.message = message;
    }

    @Override
    public String getDisplayName() {
        return this.message;
    }

    @Override
    public Composite createPageWithResult(Composite parent) {
        // Message in default page of Outline should have margins
        Composite root = new Composite(parent, SWT.NULL);
        root.setLayout(new FillLayout());

        this.messageLabel = new Label(root, SWT.LEFT | SWT.TOP | SWT.WRAP);
        this.messageLabel.setText(this.message);
        return root;
    }
}
