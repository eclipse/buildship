/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.ui.editor;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;


/**
 * @author Christophe Moine
 *
 */
public class GradlePartitionScanner extends RuleBasedPartitionScanner {
    private static class EmptyCommentDetector implements IWordDetector {

        @Override
        public boolean isWordStart(char c) {
            return (c == '/');
        }

        @Override
        public boolean isWordPart(char c) {
            return (c == '*' || c == '/');
        }
    }

    private static class EmptyCommentPredicateRule extends WordRule implements IPredicateRule {
        private final IToken successToken;

        public EmptyCommentPredicateRule(IToken successToken) {
            super(new EmptyCommentDetector());
            this.successToken = successToken;
            addWord("/**/", successToken); //$NON-NLS-1$
        }

        @Override
        public IToken evaluate(ICharacterScanner scanner, boolean resume) {
            return super.evaluate(scanner);
        }

        @Override
        public IToken getSuccessToken() {
            return this.successToken;
        }
    }

    public GradlePartitionScanner() {
        IToken jsDocToken = new Token(IGradlePartitions.GRADLEDOC);
        IToken multilineCommentToken = new Token(IGradlePartitions.MULTILINE_COMMENT);

        setPredicateRules(new IPredicateRule[] { new EndOfLineRule("//", Token.UNDEFINED), //$NON-NLS-1$
                new SingleLineRule("\"", "\"", Token.UNDEFINED, '\\'), //$NON-NLS-2$ //$NON-NLS-1$
                new SingleLineRule("'", "'", Token.UNDEFINED, '\\'), //$NON-NLS-2$ //$NON-NLS-1$
                new EmptyCommentPredicateRule(multilineCommentToken), new MultiLineRule("/**", "*/", jsDocToken, (char) 0, true), //$NON-NLS-1$ //$NON-NLS-2$
                new MultiLineRule("/*", "*/", multilineCommentToken, (char) 0, true) //$NON-NLS-1$ //$NON-NLS-2$
        });
    }
}
