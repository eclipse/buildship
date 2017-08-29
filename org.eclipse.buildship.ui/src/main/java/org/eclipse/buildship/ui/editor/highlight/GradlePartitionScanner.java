package org.eclipse.buildship.ui.editor.highlight;

import org.eclipse.jface.text.IDocument;
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
 * 
 * @author Paul
 */
public class GradlePartitionScanner extends RuleBasedPartitionScanner {

    public static final String PARTITIONING = "GradlePartitioning";
    public static final String MULTILINE_COMMENT = "__gradle_multiline_comment";
    public static final String GRADLEDOC = "__gradledoc";
    public static final String[] PARTITION_TYPES = { MULTILINE_COMMENT, GRADLEDOC };
    public static final String[] CONTENT_TYPES = { IDocument.DEFAULT_CONTENT_TYPE, GRADLEDOC, MULTILINE_COMMENT };

    private static class EmptyCommentDetector implements IWordDetector {

        public boolean isWordStart(char c) {
            return (c == '/');
        }

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

        public IToken evaluate(ICharacterScanner scanner, boolean resume) {
            return super.evaluate(scanner);
        }

        public IToken getSuccessToken() {
            return successToken;
        }
    }

    public GradlePartitionScanner() {
        IToken jsDocToken = new Token(GRADLEDOC);
        IToken multilineCommentToken = new Token(MULTILINE_COMMENT);

        setPredicateRules(new IPredicateRule[] { new EndOfLineRule("//", Token.UNDEFINED), //$NON-NLS-1$
                new SingleLineRule("\"", "\"", Token.UNDEFINED, '\\'), //$NON-NLS-2$ //$NON-NLS-1$
                new SingleLineRule("'", "'", Token.UNDEFINED, '\\'), //$NON-NLS-2$ //$NON-NLS-1$
                new EmptyCommentPredicateRule(multilineCommentToken), new MultiLineRule("/**", "*/", jsDocToken, (char) 0, true), //$NON-NLS-1$ //$NON-NLS-2$
                new MultiLineRule("/*", "*/", multilineCommentToken, (char) 0, true) //$NON-NLS-1$ //$NON-NLS-2$
        });
    }
}
