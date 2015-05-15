package org.eclipse.buildship.core.util.string;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Deque;
import java.util.List;

/**
 * Contains helper methods related to String  operations.
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Removes adjacent duplicates.
     *
     * @param elements the elements to filter
     * @return the result with adjacent duplicates removed
     */
    public static ImmutableList<String> removeAdjacentDuplicates(List<String> elements) {
        Deque<String> result = Lists.newLinkedList();
        for (String element : elements) {
            if (result.isEmpty() || !result.getLast().equals(element)) {
                result.addLast(element);
            }
        }
        return ImmutableList.copyOf(result);
    }

}
