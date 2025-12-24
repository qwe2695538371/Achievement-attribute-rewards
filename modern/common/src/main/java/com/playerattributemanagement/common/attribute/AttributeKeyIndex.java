package com.playerattributemanagement.common.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AttributeKeyIndex {
    private AttributeKeyIndex() {}

    public static List<String> orderedManagedIds(
        Collection<String> baseIds,
        Collection<String> customIds,
        Collection<String> visibilityIds
    ) {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        addAll(ordered, baseIds);
        addSorted(ordered, customIds);
        addSorted(ordered, visibilityIds);
        return new ArrayList<>(ordered);
    }

    public static Set<String> managedIdSet(
        Collection<String> baseIds,
        Collection<String> customIds,
        Collection<String> visibilityIds
    ) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        addAll(merged, baseIds);
        addAll(merged, customIds);
        addAll(merged, visibilityIds);
        return Collections.unmodifiableSet(merged);
    }

    private static void addAll(LinkedHashSet<String> target, Collection<String> values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                target.add(value);
            }
        }
    }

    private static void addSorted(LinkedHashSet<String> target, Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        List<String> sorted = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                sorted.add(value);
            }
        }
        sorted.sort(String::compareTo);
        target.addAll(sorted);
    }
}
