/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.editor;

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
 * Defines rules for {@link GradleEditor}.
 *
 * @author Christophe Moine
 */
public final class GradlePartitionScanner extends RuleBasedPartitionScanner {

    public GradlePartitionScanner() {
        IToken javadocCommentToken = new Token(GradleEditorConstants.TOKEN_TYPE_JAVADOC);
        IToken multiLineCommentToken = new Token(GradleEditorConstants.TOKEN_TYPE_MULTILINE_COMMENT);

        setPredicateRules(new IPredicateRule[] {
            new EndOfLineRule("//", Token.UNDEFINED),
            new SingleLineRule("\"", "\"", Token.UNDEFINED, '\\'),
            new SingleLineRule("'", "'", Token.UNDEFINED, '\\'),
            new EmptyCommentPredicateRule(multiLineCommentToken),
            new MultiLineRule("/**", "*/", javadocCommentToken, (char) 0, true),
            new MultiLineRule("/*", "*/", multiLineCommentToken, (char) 0, true)
        });
    }

    /**
     * Detector for empty comments.
     */
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

    /**
     * Rule for empty comments.
     */
    private static class EmptyCommentPredicateRule extends WordRule implements IPredicateRule {

        private final IToken successToken;

        public EmptyCommentPredicateRule(IToken successToken) {
            super(new EmptyCommentDetector());
            this.successToken = successToken;
            addWord("/**/", successToken);
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
}
