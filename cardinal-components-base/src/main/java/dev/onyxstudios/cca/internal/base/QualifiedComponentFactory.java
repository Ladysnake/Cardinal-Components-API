/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 Ladysnake
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dev.onyxstudios.cca.internal.base;

import com.google.common.collect.Lists;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentLoadingException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class QualifiedComponentFactory<I> {
    private static final boolean DEV = Boolean.getBoolean("fabric.development");

    private final I factory;
    private final Class<? extends Component> impl;
    private final Set<ComponentKey<?>> dependencies;
    private SortingState sortingState = SortingState.UNSORTED;

    public QualifiedComponentFactory(I factory, Class<? extends Component> impl, Set<ComponentKey<?>> dependencies) {
        this.factory = factory;
        this.impl = impl;
        this.dependencies = dependencies;
    }

    public static <I> void checkNoDependencyCycles(Map<ComponentKey<?>, QualifiedComponentFactory<I>> factories) {
        if (DEV) {
            // The result of the sort call is ignored, we are only doing this to catch errors early
            QualifiedComponentFactory.sort(factories);
        }
    }

    /**
     * Sorts factories according to their dependencies using Depth-First Search Topological Sorting
     *
     * @param factories the list of factories being sorted
     * @return a sorted list of factories
     */
    public static <I> Map<ComponentKey<?>, QualifiedComponentFactory<I>> sort(Map<ComponentKey<?>, QualifiedComponentFactory<I>> factories) {
        // reset
        factories.values().forEach(f -> f.sortingState = SortingState.UNSORTED);
        // If there is no explicit dependency, we want everything to stay in the same order
        // We are *prepending* visited nodes to the output list, so we need to visit in reverse order
        List<Map.Entry<ComponentKey<?>, QualifiedComponentFactory<I>>> in = Lists.reverse(new ArrayList<>(factories.entrySet()));
        Deque<Map.Entry<ComponentKey<?>, QualifiedComponentFactory<I>>> out = new ArrayDeque<>();
        while (!in.isEmpty()) { // can't use an iterator because sweet CME's
            visitComponentNode(in, in.get(0), out);
        }
        return out.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (c1, c2) -> c1, LinkedHashMap::new));
    }

    public static <I> void checkDependenciesSatisfied(Map<ComponentKey<?>, QualifiedComponentFactory<I>> factories) {
        RuntimeException ex = null;
        for (var checked : factories.entrySet()) {
            for (ComponentKey<?> dependency : checked.getValue().dependencies()) {
                if (!factories.containsKey(dependency)) {
                    RuntimeException ex1 = new StaticComponentLoadingException("Unsatisfied dependency for " + checked.getKey() + ": " + dependency);
                    if (ex == null) {
                        ex = ex1;
                    } else {
                        ex.addSuppressed(ex1);
                    }
                }
            }
        }
        if (ex != null) throw ex;
    }

    private static <I> void visitComponentNode(List<Map.Entry<ComponentKey<?>, QualifiedComponentFactory<I>>> factories, Map.Entry<ComponentKey<?>, QualifiedComponentFactory<I>> node, Deque<Map.Entry<ComponentKey<?>, QualifiedComponentFactory<I>>> out) {
        switch (node.getValue().sortingState) {
            case SORTED -> {}
            case SORTING -> throw new StaticComponentLoadingException("Circular dependency detected: " + node.getKey());
            case UNSORTED -> {
                node.getValue().sortingState = SortingState.SORTING;
                // OPTI: indexing dependencies beforehand would let us run in linear time
                try {
                    // Beware, CME's ahoy
                    for (var dependant : factories.stream().filter(entry -> entry.getValue().dependencies().contains(node.getKey())).toList()) {
                        visitComponentNode(factories, dependant, out);
                    }
                } catch (StaticComponentLoadingException e) {
                    throw new StaticComponentLoadingException(e.getMessage() + " <- " + node.getKey());
                }
                factories.remove(node);
                node.getValue().sortingState = SortingState.SORTED;
                out.addFirst(node);
            }
        }
    }

    public I factory() {
        return factory;
    }

    public Class<? extends Component> impl() {
        return impl;
    }

    public Set<ComponentKey<?>> dependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return "QualifiedComponentFactory[" +
            "factory=" + factory + ", " +
            "impl=" + impl + ", " +
            "dependencies=" + dependencies + ']';
    }

    enum SortingState {
        UNSORTED, SORTING, SORTED
    }
}
