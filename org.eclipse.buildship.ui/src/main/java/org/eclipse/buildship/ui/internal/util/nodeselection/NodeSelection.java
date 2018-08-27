/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 479243
 */

package org.eclipse.buildship.ui.internal.util.nodeselection;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import java.util.Iterator;
import java.util.List;

/**
 * Provides information about a given set of selected nodes.
 */
public final class NodeSelection implements IStructuredSelection {

    private static final NodeSelection EMPTY = new NodeSelection(ImmutableList.of());

    private final ImmutableList<?> nodes;

    private NodeSelection(List<?> nodes) {
        this.nodes = ImmutableList.copyOf(nodes);
    }

    /**
     * Returns whether the selection is empty or not.
     *
     * @return {code true} if the selection is empty
     */
    @Override
    public boolean isEmpty() {
        return this.nodes.isEmpty();
    }

    /**
     * Returns whether a single node is selected.
     *
     * @return {@code true} if a single node is selected, {@code false} otherwise
     */
    public boolean isSingleSelection() {
        return size() == 1;
    }

    @Override
    public Object getFirstElement() {
        return isEmpty() ? null : this.nodes.get(0);
    }

    /**
     * Returns the first node where the first node is expected to be of the given type.
     *
     * @param expectedType the expected type of the first node
     * @return the first node
     * @throws java.lang.IllegalStateException thrown if the selection is empty
     */
    public <T> T getFirstElement(Class<T> expectedType) {
        if (isEmpty()) {
            throw new IllegalStateException("Selection is empty.");
        } else {
            return expectedType.cast(this.nodes.get(0));
        }
    }

    /**
     * Returns a list of all nodes where all nodes are expected to be of the given type.
     *
     * @param expectedType the expected type of all nodes
     * @return the list of all nodes
     * @throws ClassCastException thrown if a node is not of the expected type
     */
    public <T> ImmutableList<T> toList(final Class<T> expectedType) {
        return FluentIterable.from(this.nodes).transform(new Function<Object, T>() {

            @Override
            public T apply(Object input) {
                return expectedType.cast(input);
            }
        }).toList();
    }

    @Override
    public Iterator<?> iterator() {
        return this.nodes.iterator();
    }

    @Override
    public int size() {
        return this.nodes.size();
    }

    @Override
    public Object[] toArray() {
        return this.nodes.toArray();
    }

    @Override
    public List<?> toList() {
        return this.nodes;
    }

    /**
     * Checks whether all nodes are of the given type.
     *
     * @param expectedType the expected type of the nodes
     * @return {@code true} if all nodes match the type
     */
    public boolean hasAllNodesOfType(Class<?> expectedType) {
        return allMatch(Predicates.instanceOf(expectedType));
    }

    /**
     * Checks whether all nodes of this node selection meet the specified criteria.
     *
     * @param predicate the criteria to match
     * @return {@code true} if all nodes match the criteria
     */
    public boolean allMatch(Predicate<Object> predicate) {
        return FluentIterable.from(this.nodes).allMatch(predicate);
    }

    /**
     * Merges this node selection with the given node selection by removing those nodes that are not part of the new selection and by adding those nodes that are only in the new
     * selection. The merge result is returned as a new {@code NodeSelection} instance, while this node selection is not modified.
     *
     * @param newSelection the node selection to merge with
     * @return a new instance containing the merge result
     */
    public NodeSelection mergeWith(NodeSelection newSelection) {
        // short-circuit if the new selection is empty
        if (newSelection.isEmpty()) {
            return NodeSelection.empty();
        }

        List<Object> result = Lists.newArrayList(this.nodes);

        // remove those nodes that are not in the new selection anymore
        result.retainAll(newSelection.toList());

        // add those nodes that are new in the new selection
        ImmutableList<?> newlySelected = removeAll(newSelection.toList(), result);
        result.addAll(newlySelected);

        return new NodeSelection(result);
    }

    private ImmutableList<?> removeAll(List<?> toRemoveFrom, final List<?> elementsToRemove) {
        return FluentIterable.from(toRemoveFrom).filter(new Predicate<Object>() {

            @Override
            public boolean apply(Object node) {
                return !elementsToRemove.contains(node);
            }
        }).toList();
    }

    /**
     * Creates a new instance representing the empty selection.
     *
     * @return the new instance
     */
    public static NodeSelection empty() {
        return EMPTY;
    }

    public static NodeSelection single(Object object) {
        return new NodeSelection(ImmutableList.of(object));
    }

    /**
     * Creates a new instance reflecting the given {@link IStructuredSelection} instance.
     *
     * @param selection the selection from which to create the new instance
     * @return the new instance
     */
    public static NodeSelection from(IStructuredSelection selection) {
        return selection.isEmpty() ? empty() : new NodeSelection(selection.toList());
    }

    /**
     * Creates a new instance reflecting the given {@link ISelection} instance. All selection sub-types other than the {@link IStructuredSelection} sub-type will always have an
     * empty {@link NodeSelection} instance returned.
     *
     * @param selection the selection from which to create the new instance
     * @return the new instance
     */
    public static NodeSelection from(ISelection selection) {
        return selection instanceof IStructuredSelection ? from((IStructuredSelection) selection) : empty();
    }

}
