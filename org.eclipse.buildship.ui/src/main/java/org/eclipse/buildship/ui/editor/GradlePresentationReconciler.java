/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.ui.editor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
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

import org.eclipse.buildship.core.util.preference.EclipsePreferencesUtils;

/**
 * Presentation Reconcilier for the Gradle Editor
 *
 * @author Christophe Moine
 */
public class GradlePresentationReconciler extends PresentationReconciler {
    private static final List<String> GRADLE_IDENTIFIERS = Arrays.asList("assert", "if", "else", "void", "null", "new", "return", "try", "catch", "def", "allprojects", //
            "artifacts", "buildscript", "configurations", "dependencies", "repositories", "sourceSets", "subprojects", "publishing", "task", "apply", "sourceCompatibility", //
            "targetCompatibility", "test", "project", "ext", "plugins", "jar", "shadowJar", "for", "while");

    private final Map<String, Token> idToTokens=ImmutableMap.<String, Token>builder()
            .put(IJavaColorConstants.JAVA_KEYWORD, new Token(null)) //
            .put(IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT, new Token(null)) //
            .put(IJavaColorConstants.JAVA_MULTI_LINE_COMMENT, new Token(null)) //
            .put(IJavaColorConstants.JAVA_STRING, new Token(null)) //
            .put(IJavaColorConstants.JAVA_DEFAULT, new Token(null)) //
            .build();

    private IEclipsePreferences preferencesNode = EclipsePreferencesUtils.getInstanceScope().getNode(JavaUI.ID_PLUGIN);

    @Override
    public void install(final ITextViewer viewer) {
        this.preferencesNode.addPreferenceChangeListener(new IPreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent event) {
                if(updateToken(event.getKey())) {
                    viewer.invalidateTextPresentation();
                }
            }
        });
        for(Entry<String, Token> entry: this.idToTokens.entrySet()) {
            updateToken(entry.getKey());
        }

        BufferedRuleBasedScanner scanner = new BufferedRuleBasedScanner() {};
        scanner.setRules(new IRule[] { new EndOfLineRule("//", this.idToTokens.get(IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT)), // $NON-NLS-2$
                new MultiLineRule("/*", "*/", this.idToTokens.get(IJavaColorConstants.JAVA_MULTI_LINE_COMMENT), (char) 0, true), //$NON-NLS-2$
                new SingleLineRule("\"", "\"", this.idToTokens.get(IJavaColorConstants.JAVA_STRING)), //
                new SingleLineRule("'", "'", this.idToTokens.get(IJavaColorConstants.JAVA_STRING)), //
                new KeywordRule(this.idToTokens.get(IJavaColorConstants.JAVA_KEYWORD)), //
                new WordRule(new WordDetector(), this.idToTokens.get(IJavaColorConstants.JAVA_DEFAULT)),
                new NumberRule(this.idToTokens.get(IJavaColorConstants.JAVA_DEFAULT)),
        });
        setDamagerRepairer(scanner, IDocument.DEFAULT_CONTENT_TYPE);
        MultilineCommentScanner multilineCommentScanner=new MultilineCommentScanner(this.idToTokens.get(IJavaColorConstants.JAVA_MULTI_LINE_COMMENT));
        setDamagerRepairer(multilineCommentScanner, IGradlePartitions.MULTILINE_COMMENT);
        setDamagerRepairer(multilineCommentScanner, IGradlePartitions.GRADLEDOC);

        super.install(viewer);
    }

    private void setDamagerRepairer(ITokenScanner scanner, String tokenType) {
        DefaultDamagerRepairer damagerRepairer = new DefaultDamagerRepairer(scanner);
        setDamager(damagerRepairer, tokenType);
        setRepairer(damagerRepairer, tokenType);
    }

    private boolean updateToken(String key) {
        Token token = this.idToTokens.get(key);
        if(token!=null) {
            token.setData(new TextAttribute(JavaUI.getColorManager().getColor(key)));
        }
        return token!=null;
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
