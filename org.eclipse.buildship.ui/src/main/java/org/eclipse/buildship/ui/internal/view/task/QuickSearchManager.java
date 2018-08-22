/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.internal.view.task;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Custom label control wrapper hooking quick search capability to a {@link Tree}.
 *
 * When instantiated, it adds a key listener to the tree which performs a search amongst the tree
 * items based on the entered search pattern. The matching results can be traversed with the up/down
 * keys.
 * <p>
 * The implementation is wrapped around a {@link Label} control that displays the search pattern.
 * <p>
 * If there is nothing entered or the enter/escape key is pressed, the up/down keys regain their
 * original functions (e.g. navigating between all nodes).
 */
public final class QuickSearchManager {

    // the target tree on which to support quick searches
    private final Tree tree;

    // shows the current search text
    private final Label label;

    // holds the current search state
    private final QuickSearchState state;

    // updates the search text and selection when a key is pressed while the tree is in focus
    private final KeyListener listener;

    public QuickSearchManager(Tree tree, Label label) {
        this.tree = Preconditions.checkNotNull(tree);
        this.label = Preconditions.checkNotNull(label);
        this.state = new QuickSearchState();
        this.listener = new TreeKeyListener();

        init();
    }

    private void init() {
        this.tree.addKeyListener(this.listener);
        reset(false);
    }

    public void reset() {
        reset(true);
    }

    private void reset(boolean resetTreeSelection) {
        this.state.reset();
        updateLabelText();
        if (resetTreeSelection) {
            updateTreeSelection(true);
        }
    }

    private void handleDownArrow(KeyEvent e) {
        // if the search is active, move the selection to the next match
        if (isSearchActive()) {
            if (this.state.results.length > 0) {
                this.state.current++;
                this.state.current = this.state.current % this.state.results.length;

                updateTreeSelection(false);
            }

            // disable event propagation (don't move the cursor one down)
            e.doit = false;
        }
    }

    private void handleUpArrow(KeyEvent e) {
        // if the search is active, move the selection to the previous match
        if (isSearchActive()) {
            if (this.state.results.length > 0) {
                this.state.current--;
                if (this.state.current < 0) {
                    this.state.current += this.state.results.length;
                }

                updateTreeSelection(false);
            }

            // disable event propagation (don't move the cursor one up)
            e.doit = false;
        }
    }

    private void handleBackspace(KeyEvent e) {
        // if the search is active, remove the last character from the search text and update the
        // search results
        if (isSearchActive()) {
            // remove the last character from the search if any
            this.state.removeLastCharacterFromSearchText();

            if (isSearchActive()) {
                // update the search text and search results
                performSearch();

                // reflect the search result in the ui
                updateLabelText();
                updateTreeSelection(true);
            } else {
                // clear the search text and search results
                reset(false);
            }

            // disable event propagation (don't let Eclipse's default key listener take action)
            e.doit = false;
        }
    }

    private void handleMiscKeys(KeyEvent e) {
        // if the input is a valid character, append it to the search text and update the search
        // results
        if (Character.isLetterOrDigit(e.character)) {
            // update the state of the search text and perform the search logic
            this.state.appendToSearchText(e);
            performSearch();

            // reflect the search result in the ui
            updateLabelText();
            updateTreeSelection(true);

            // disable event propagation (don't let Eclipse's default key listener take action)
            e.doit = false;
        }
    }

    private boolean isSearchActive() {
        // the search is active if the search text is non-empty
        // if the search is not active, the cursors can be used to traverse the tree
        // if the search is active, the traversal is bound to the matching tree items
        return !Strings.isNullOrEmpty(this.state.searchText);
    }

    private void performSearch() {
        // iterate through all tree items and find the ones matching the search pattern
        final String searchText = QuickSearchManager.this.state.searchText.toUpperCase();
        Predicate<TreeItem> predicate = new Predicate<TreeItem>() {

            @Override
            public boolean apply(TreeItem item) {
                return item.getText().toUpperCase().contains(searchText);
            }
        };
        ImmutableList.Builder<TreeItem> hits = ImmutableList.builder();
        filterRecursively(ImmutableList.copyOf(this.tree.getItems()), predicate, hits);
        this.state.setResults(hits.build());
    }

    private void filterRecursively(ImmutableList<TreeItem> items, Predicate<TreeItem> predicate, ImmutableList.Builder<TreeItem> hits) {
        for (TreeItem item : items) {
            if (predicate.apply(item)) {
                hits.add(item);
            }

            // do not traverse into closed nodes
            if (item.getExpanded()) {
                TreeItem[] childItems = item.getItems();
                filterRecursively(ImmutableList.copyOf(childItems), predicate, hits);
            }
        }
    }

    private void updateLabelText() {
        // update the label to show how many matching elements are there
        if (isSearchActive()) {
            this.label.setText(String.format("%s (%d matches)", this.state.searchText, this.state.results.length));
        } else {
            this.label.setText("Type to search, use arrows to navigate");
        }
    }

    private void updateTreeSelection(boolean clearIfNoResults) {
        if (this.state.results.length > 0) {
            // if there are matching items, select the current one
            TreeItem item = this.state.results[this.state.current];
            this.tree.setSelection(item);
            this.tree.notifyListeners(SWT.Selection, new Event());
        } else if (clearIfNoResults) {
            // if there are no matching items, clear the selection
            this.tree.setSelection(new TreeItem[0]);
            this.tree.notifyListeners(SWT.Selection, new Event());
        }

        // when we update the selection, we disable the event propagation
        // (KeyEvent.doit = false), as a consequence, the selection is not
        // propagated properly towards the listeners, thus raise a
        // selection event manually
    }

    public void dispose() {
        if (!this.tree.isDisposed()) { // guard required
            this.tree.removeKeyListener(this.listener);
        }
    }

    /**
     * Encapsulates the current state of the quick search.
     */
    private final class QuickSearchState {

        // contains the current search text
        private String searchText;

        // the tree items that result from applying the current search text
        private TreeItem[] results;

        // the index of the current item in the list of matching tree items
        private int current;

        private QuickSearchState() {
            reset();
        }

        private void reset() {
            this.searchText = "";
            this.results = new TreeItem[0];
            this.current = -1;
        }

        private void appendToSearchText(KeyEvent e) {
            this.searchText += e.character;
        }

        private void removeLastCharacterFromSearchText() {
            String searchText = this.searchText;
            if (searchText.length() > 0) {
                this.searchText = searchText.substring(0, searchText.length() - 1);
            }
        }

        private void setResults(List<TreeItem> hits) {
            this.results = Iterables.toArray(hits, TreeItem.class);
            this.current = this.results.length > 0 ? 0 : -1;
        }

    }

    /**
     * {code KeyListener} instance that, for each key stroke, updates the quick search accordingly.
     */
    private final class TreeKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.keyCode) {
                case SWT.CR:
                    reset(false); // keep current selection
                    break;
                case SWT.ESC:
                    reset(true); // clear current selection
                    break;
                case SWT.ARROW_DOWN:
                    handleDownArrow(e);
                    break;
                case SWT.ARROW_UP:
                    handleUpArrow(e);
                    break;
                case SWT.BS:
                    handleBackspace(e);
                    break;
                default:
                    handleMiscKeys(e);
                    break;
            }
        }

    }

}
