package org.eclipse.buildship.ui.editor;

import java.util.Arrays;
import java.util.List;

import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.preferences.GradleEditorConstants;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;

public class GradlePresentationReconciler extends PresentationReconciler {
	private static final List<String> GRADLE_IDENTIFIERS=Arrays.asList(
			// Java keywords
	        "assert",
	        "if", "else",
	        "void", "null",
	        "new", "return",
	        "try", "catch",

	        // Groovy stuff
	        "def",

			// Gradle DSL http://gradle.org/docs/current/dsl/ {
	        // Build script blocks
	        "allprojects", "artifacts", "buildscript", "configurations",
	        "dependencies", "repositories", "sourceSets", "subprojects", "publishing",
			"task", "apply", "sourceCompatibility", "targetCompatibility",
	        "test",

	        "project",
	        "ext",
	        "plugins", //since 2.1
	        //}

	        // apply plugin: 'java'
	        // apply plugin: 'com.github.johnrengelman.shadow'
	        "jar", "shadowJar"
	);

	private final TextAttribute commentAttribute = new TextAttribute(EditorColors.getColor(GradleEditorConstants.KEY_COLOR_COMMENT), null, SWT.NORMAL);
	private final TextAttribute docAttribute = new TextAttribute(EditorColors.getColor(GradleEditorConstants.KEY_COLOR_DOC), null, SWT.NORMAL);
	private final TextAttribute keywordAttribute = new TextAttribute(EditorColors.getColor(GradleEditorConstants.KEY_COLOR_KEYWORD), null,
			UiPlugin.getInstance().getPreferenceStore().getBoolean(GradleEditorConstants.KEY_BOLD_KEYWORD) ? SWT.BOLD : SWT.NORMAL);
	private final TextAttribute stringAttribute = new TextAttribute(EditorColors.getColor(GradleEditorConstants.KEY_COLOR_STRING), null, SWT.NORMAL);
	private final TextAttribute numberAttribute = new TextAttribute(EditorColors.getColor(GradleEditorConstants.KEY_COLOR_NUMBER), null, SWT.NORMAL);
	private final TextAttribute normalAttribute = new TextAttribute(EditorColors.getColor(GradleEditorConstants.KEY_COLOR_NORMAL), null, SWT.NORMAL);

	private final IToken keywordToken = new Token(this.keywordAttribute);
	private final IToken docToken = new Token(this.docAttribute);
	private final IToken quoteToken = new Token(this.stringAttribute);
	private final IToken numberToken = new Token(this.numberAttribute);
	private final IToken commentToken = new Token(this.commentAttribute);
	private final IToken normalToken = new Token(this.normalAttribute);

	public GradlePresentationReconciler() {
		RuleBasedScanner scanner = new RuleBasedScanner();
		scanner.setRules(new IRule[] {
				new EndOfLineRule("//", this.commentToken),//$NON-NLS-2$
				new KeywordRule(this.keywordToken),
				new MultiLineRule("/**", "*/", this.docToken, (char) 0, false), //$NON-NLS-2$
				new MultiLineRule("/*", "*/", this.commentToken, (char) 0, false), //$NON-NLS-2$
				new SingleLineRule("\"","\"", this.quoteToken),
				new SingleLineRule("'", "'", this.quoteToken),
				new WhitespaceRule(new IWhitespaceDetector() {
                    @Override
					public boolean isWhitespace(char c) {
                        return Character.isWhitespace(c);
                    }
                }),
				new WordRule(new WordDetector(), this.normalToken),
                new NumberRule(this.numberToken),
		});

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
		this.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		this.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
	}

	private static class KeywordRule extends WordRule {

        public KeywordRule(IToken token) {
            super(new WordDetector());
            for (String word : GRADLE_IDENTIFIERS) {
                addWord(word, token);
            }
        }
    }

    private static class WordDetector implements IWordDetector {

        @Override
		public boolean isWordPart(char c) {
            return Character.isJavaIdentifierPart(c);
        }

        @Override
		public boolean isWordStart(char c) {
            return Character.isJavaIdentifierStart(c);
        }

    }
}
