/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.ui.editor;

import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * @author Christophe Moine
 *
 */
public class MultilineCommentScanner extends BufferedRuleBasedScanner {
    public MultilineCommentScanner(Token token) {
        setDefaultReturnToken(token);
    }
}