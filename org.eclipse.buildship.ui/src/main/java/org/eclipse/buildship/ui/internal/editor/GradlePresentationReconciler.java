/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.ui.internal.editor;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

import org.eclipse.buildship.core.internal.util.preference.EclipsePreferencesUtils;

/**
 * Presentation reconciler for the Gradle Editor.
 *
 * @author Christophe Moine
 */
public final class GradlePresentationReconciler extends PresentationReconciler {

    private TokenUpdatingPreferenceChangeListener listener;

    @Override
    public void install(final ITextViewer viewer) {
        super.install(viewer);

        BuildScriptTokens tokens = new BuildScriptTokens();
        tokens.update();

        this.listener = new TokenUpdatingPreferenceChangeListener(viewer, tokens);
        EclipsePreferencesUtils.getInstanceScope().getNode(JavaUI.ID_PLUGIN).addPreferenceChangeListener(this.listener);

        SimpleScanner scanner = new SimpleScanner();
        scanner.setRules(tokens.toRules());
        setDamagerRepairer(scanner, IDocument.DEFAULT_CONTENT_TYPE);

        MultilineCommentScanner multilineCommentScanner = new MultilineCommentScanner(tokens.getToken(BuildScriptTokenType.MULTI_LINE_COMMENT));
        setDamagerRepairer(multilineCommentScanner, GradleEditorConstants.TOKEN_TYPE_MULTILINE_COMMENT);
        setDamagerRepairer(multilineCommentScanner, GradleEditorConstants.TOKEN_TYPE_JAVADOC);
    }

    @Override
    public void uninstall() {
        if (this.listener != null) {
            EclipsePreferencesUtils.getInstanceScope().getNode(JavaUI.ID_PLUGIN).removePreferenceChangeListener(this.listener);
        }
        super.uninstall();
    }

    private void setDamagerRepairer(ITokenScanner scanner, String tokenType) {
        DefaultDamagerRepairer damagerRepairer = new DefaultDamagerRepairer(scanner);
        setDamager(damagerRepairer, tokenType);
        setRepairer(damagerRepairer, tokenType);
    }

    /**
     * Tokens types available in the Gradle editor.
     */
    private enum BuildScriptTokenType {
        KEYWORD(IJavaColorConstants.JAVA_KEYWORD),
        SINGLE_LINE_COMMENT(IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT),
        MULTI_LINE_COMMENT(IJavaColorConstants.JAVA_MULTI_LINE_COMMENT),
        STRING(IJavaColorConstants.JAVA_STRING),
        DEFAULT(IJavaColorConstants.JAVA_DEFAULT);

        private final String colorKey;

        BuildScriptTokenType(String colorKey) {
            this.colorKey = colorKey;
        }

        public String getColorKey() {
            return this.colorKey;
        }

        private static List<String> getAllColorKeys() {
            List<String> result = Lists.newArrayListWithCapacity(BuildScriptTokenType.values().length);
            for (BuildScriptTokenType tokenType : BuildScriptTokenType.values()) {
                result.add(tokenType.getColorKey());
            }
            return result;
        }
    }

    /**
     * Tokens for the current editor.
     */
    private static class BuildScriptTokens {

        private final ImmutableMap<BuildScriptTokenType, Token> tokens;

        public BuildScriptTokens() {
            Builder<BuildScriptTokenType, Token> result = ImmutableMap.<BuildScriptTokenType,Token>builder();
            for (BuildScriptTokenType tokenType : BuildScriptTokenType.values()) {
                result.put(tokenType, new Token(null));
            }
            this.tokens = result.build();
        }

        public Token getToken(BuildScriptTokenType tokenType) {
            return this.tokens.get(tokenType);
        }

        public void update() {
            for (BuildScriptTokenType tokenType : BuildScriptTokenType.values()) {
                this.tokens.get(tokenType).setData(new TextAttribute(JavaUI.getColorManager().getColor(tokenType.getColorKey())));
            }
        }

        public IRule[] toRules() {
            return new IRule[] { new EndOfLineRule("//", this.tokens.get(BuildScriptTokenType.SINGLE_LINE_COMMENT)),
                new MultiLineRule("/*", "*/", this.tokens.get(BuildScriptTokenType.MULTI_LINE_COMMENT), (char) 0, true),
                new SingleLineRule("\"", "\"", this.tokens.get(BuildScriptTokenType.STRING)), new SingleLineRule("'", "'", this.tokens.get(BuildScriptTokenType.STRING)),
                new KeywordRule(this.tokens.get(BuildScriptTokenType.KEYWORD)), //
                new WordRule(new WordDetector(), this.tokens.get(BuildScriptTokenType.DEFAULT)), new NumberRule(this.tokens.get(BuildScriptTokenType.DEFAULT)) };
        }
    }

    /**
     * Rule for keywords.
     */
    private static class KeywordRule extends WordRule {

        private static final List<String> KEYWORDS = ImmutableList.of(
            "assert", "if", "else", "void", "null", "new", "return", "try", "catch", "def", "allprojects", "artifacts",
            "buildscript", "configurations", "dependencies", "repositories", "sourceSets", "subprojects", "publishing",
            "task", "apply", "sourceCompatibility", "targetCompatibility", "test", "project", "ext", "plugins", "jar",
            "shadowJar", "for", "while"
        );

        public KeywordRule(IToken token) {
            super(new WordDetector());
            for (String word : KEYWORDS) {
                addWord(word, token);
            }
        }
    }

    /**
     * Word detector.
     */
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

    /**
     * Same as BufferedRuleBasedScanner but with public constructor.
     */
    private static class SimpleScanner extends BufferedRuleBasedScanner {
        //
    }

    /**
     * Scanner for multi-line comments.
     */
    private static class MultilineCommentScanner extends BufferedRuleBasedScanner {

        public MultilineCommentScanner(Token token) {
            setDefaultReturnToken(token);
        }
    }

    /**
     * Updates the editor colors when the color preference changes.
     */
    private static class TokenUpdatingPreferenceChangeListener implements IPreferenceChangeListener {

        private final ITextViewer viewer;
        private final BuildScriptTokens tokens;

        public TokenUpdatingPreferenceChangeListener(ITextViewer viewer, BuildScriptTokens tokens) {
            this.viewer = viewer;
            this.tokens = tokens;
        }

        @Override
        public void preferenceChange(PreferenceChangeEvent event) {
            if (BuildScriptTokenType.getAllColorKeys().contains(event.getKey())) {
                this.viewer.invalidateTextPresentation();
                this.tokens.update();
            }
        }
    }
}
