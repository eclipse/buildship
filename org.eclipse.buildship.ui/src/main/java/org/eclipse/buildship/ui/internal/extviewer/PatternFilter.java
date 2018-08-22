/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.buildship.ui.internal.extviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.icu.text.BreakIterator;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Based on org.eclipse.ui.dialogs.PatternFilter.
 */
public class PatternFilter extends ViewerFilter {

    /*
     * Cache of filtered elements in the tree
     */
    private Map<Object, Object[]> cache = new HashMap<Object, Object[]>();

    /*
     * Maps parent elements to TRUE or FALSE
     */
    private Map<Object, Boolean> foundAnyCache = new HashMap<Object, Boolean>();

    private boolean useCache = false;

    /**
     * Whether to include a leading wildcard for all provided patterns. A trailing wildcard is
     * always included.
     */
    private boolean includeLeadingWildcard = false;

    /**
     * The string pattern matcher used for this pattern filter.
     */
    private StringMatcher matcher;

    private boolean useEarlyReturnIfMatcherIsNull = true;

    private static Object[] EMPTY = new Object[0];

    public PatternFilter() {
        this(false);
    }

    public PatternFilter(boolean includeLeadingWildcard) {
        setIncludeLeadingWildcard(includeLeadingWildcard);
    }

    @Override
    public final Object[] filter(Viewer viewer, Object parent, Object[] elements) {
        // we don't want to optimize if we've extended the filter ... this
        // needs to be addressed in 3.4
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=186404
        if (this.matcher == null && this.useEarlyReturnIfMatcherIsNull) {
            return elements;
        }

        if (!this.useCache) {
            return super.filter(viewer, parent, elements);
        }

        Object[] filtered = this.cache.get(parent);
        if (filtered == null) {
            Boolean foundAny = this.foundAnyCache.get(parent);
            if (foundAny != null && !foundAny.booleanValue()) {
                filtered = EMPTY;
            } else {
                filtered = super.filter(viewer, parent, elements);
            }
            this.cache.put(parent, filtered);
        }
        return filtered;
    }

    /**
     * Returns true if any of the elements makes it through the filter. This method uses caching if
     * enabled; the computation is done in computeAnyVisible.
     *
     * @param viewer
     * @param parent
     * @param elements the elements (must not be an empty array)
     * @return true if any of the elements makes it through the filter.
     */
    private boolean isAnyVisible(Viewer viewer, Object parent, Object[] elements) {
        if (this.matcher == null) {
            return true;
        }

        if (!this.useCache) {
            return computeAnyVisible(viewer, elements);
        }

        Object[] filtered = this.cache.get(parent);
        if (filtered != null) {
            return filtered.length > 0;
        }
        Boolean foundAny = this.foundAnyCache.get(parent);
        if (foundAny == null) {
            foundAny = computeAnyVisible(viewer, elements) ? Boolean.TRUE : Boolean.FALSE;
            this.foundAnyCache.put(parent, foundAny);
        }
        return foundAny.booleanValue();
    }

    /**
     * Returns true if any of the elements makes it through the filter.
     *
     * @param viewer the viewer
     * @param elements the elements to test
     * @return <code>true</code> if any of the elements makes it through the filter
     */
    private boolean computeAnyVisible(Viewer viewer, Object[] elements) {
        boolean elementFound = false;
        for (int i = 0; i < elements.length && !elementFound; i++) {
            Object element = elements[i];
            elementFound = isElementVisible(viewer, element);
        }
        return elementFound;
    }

    @Override
    public final boolean select(Viewer viewer, Object parentElement, Object element) {
        return isElementVisible(viewer, element);
    }

    /**
     * Sets whether a leading wildcard should be attached to each pattern string.
     *
     * @param includeLeadingWildcard Whether a leading wildcard should be added.
     */
    public final void setIncludeLeadingWildcard(final boolean includeLeadingWildcard) {
        this.includeLeadingWildcard = includeLeadingWildcard;
    }

    /**
     * The pattern string for which this filter should select elements in the viewer.
     *
     * @param patternString
     */
    public void setPattern(String patternString) {
        // these 2 strings allow the PatternFilter to be extended in
        // 3.3 - https://bugs.eclipse.org/bugs/show_bug.cgi?id=186404
        if ("org.eclipse.ui.keys.optimization.true".equals(patternString)) { //$NON-NLS-1$
            this.useEarlyReturnIfMatcherIsNull = true;
            return;
        } else if ("org.eclipse.ui.keys.optimization.false".equals(patternString)) { //$NON-NLS-1$
            this.useEarlyReturnIfMatcherIsNull = false;
            return;
        }
        clearCaches();
        if (patternString == null || patternString.equals("")) { //$NON-NLS-1$
            this.matcher = null;
        } else {
            String pattern = patternString + "*"; //$NON-NLS-1$
            if (this.includeLeadingWildcard) {
                pattern = "*" + pattern; //$NON-NLS-1$
            }
            this.matcher = new StringMatcher(pattern, true, false);
        }
    }

    /**
     * Clears the caches used for optimizing this filter. Needs to be called whenever the tree
     * content changes.
     */
    /* package */void clearCaches() {
        this.cache.clear();
        this.foundAnyCache.clear();
    }

    /**
     * Answers whether the given String matches the pattern.
     *
     * @param string the String to test
     *
     * @return whether the string matches the pattern
     */
    private boolean match(String string) {
        if (this.matcher == null) {
            return true;
        }
        return this.matcher.match(string);
    }

    /**
     * Answers whether the given element is a valid selection in the filtered tree. For example, if
     * a tree has items that are categorized, the category itself may not be a valid selection since
     * it is used merely to organize the elements.
     *
     * @param element
     * @return true if this element is eligible for automatic selection
     */
    public boolean isElementSelectable(Object element) {
        return element != null;
    }

    /**
     * Answers whether the given element in the given viewer matches the filter pattern. This is a
     * default implementation that will show a leaf element in the tree based on whether the
     * provided filter text matches the text of the given element's text, or that of it's children
     * (if the element has any).
     *
     * Subclasses may override this method.
     *
     * @param viewer the tree viewer in which the element resides
     * @param element the element in the tree to check for a match
     *
     * @return true if the element matches the filter pattern
     */
    public boolean isElementVisible(Viewer viewer, Object element) {
        return isParentMatch(viewer, element) || isLeafMatch(viewer, element);
    }

    /**
     * Check if the parent (category) is a match to the filter text. The default behavior returns
     * true if the element has at least one child element that is a match with the filter text.
     *
     * Subclasses may override this method.
     *
     * @param viewer the viewer that contains the element
     * @param element the tree element to check
     * @return true if the given element has children that matches the filter text
     */
    protected boolean isParentMatch(Viewer viewer, Object element) {
        Object[] children = ((ITreeContentProvider) ((AbstractTreeViewer) viewer).getContentProvider()).getChildren(element);

        if ((children != null) && (children.length > 0)) {
            return isAnyVisible(viewer, element, children);
        }
        return false;
    }

    /**
     * Check if the current (leaf) element is a match with the filter text. The default behavior
     * checks that the label of the element is a match.
     *
     * Subclasses should override this method.
     *
     * @param viewer the viewer that contains the element
     * @param element the tree element to check
     * @return true if the given element's label matches the filter text
     */
    protected boolean isLeafMatch(Viewer viewer, Object element) {
        // check for CellLabelProvider, which are also ILabelProvider,
        // e.g., ColumnLabelProvider
        CellLabelProvider cellLabelProvider = null;
        if (viewer instanceof ColumnViewer) {
            cellLabelProvider = ((ColumnViewer) viewer).getLabelProvider(0);
        }
        String labelText = getTextFromLabelProvider(cellLabelProvider, element);

        if (labelText == null) {
            IBaseLabelProvider baseLabelProvider = ((StructuredViewer) viewer).getLabelProvider();
            labelText = getTextFromLabelProvider(baseLabelProvider, element);
        }
        return wordMatches(labelText);
    }

    private String getTextFromLabelProvider(IBaseLabelProvider baseLabelProvider, Object element) {
        if (baseLabelProvider == null) {
            return null;
        }
        String labelText = null;
        if (baseLabelProvider instanceof ILabelProvider) {
            labelText = ((ILabelProvider) baseLabelProvider).getText(element);
        } else if (baseLabelProvider instanceof IStyledLabelProvider) {
            labelText = ((IStyledLabelProvider) baseLabelProvider).getStyledText(element).getString();
        } else if (baseLabelProvider instanceof DelegatingStyledCellLabelProvider) {
            IStyledLabelProvider styledStringProvider = ((DelegatingStyledCellLabelProvider) baseLabelProvider).getStyledStringProvider();
            StyledString styledText = styledStringProvider.getStyledText(element);
            if (styledText != null) {
                labelText = styledText.getString();
            }
        }

        return labelText;
    }

    /**
     * Take the given filter text and break it down into words using a BreakIterator.
     *
     * @param text
     * @return an array of words
     */
    private String[] getWords(String text) {
        List<String> words = new ArrayList<String>();
        // Break the text up into words, separating based on whitespace and
        // common punctuation.
        // Previously used String.split(..., "\\W"), where "\W" is a regular
        // expression (see the Javadoc for class Pattern).
        // Need to avoid both String.split and regular expressions, in order to
        // compile against JCL Foundation (bug 80053).
        // Also need to do this in an NL-sensitive way. The use of BreakIterator
        // was suggested in bug 90579.
        BreakIterator iter = BreakIterator.getWordInstance();
        iter.setText(text);
        int i = iter.first();
        while (i != java.text.BreakIterator.DONE && i < text.length()) {
            int j = iter.following(i);
            if (j == java.text.BreakIterator.DONE) {
                j = text.length();
            }
            // match the word
            if (Character.isLetterOrDigit(text.charAt(i))) {
                String word = text.substring(i, j);
                words.add(word);
            }
            i = j;
        }
        return words.toArray(new String[words.size()]);
    }

    /**
     * Return whether or not if any of the words in text satisfy the match critera.
     *
     * @param text the text to match
     * @return boolean <code>true</code> if one of the words in text satisifes the match criteria.
     */
    protected boolean wordMatches(String text) {
        if (text == null) {
            return false;
        }

        // If the whole text matches we are all set
        if (match(text)) {
            return true;
        }

        // Otherwise check if any of the words of the text matches
        String[] words = getWords(text);
        for (String word : words) {
            if (match(word)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Can be called by the filtered tree to turn on caching.
     *
     * @param useCache The useCache to set.
     */
    void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }
}
