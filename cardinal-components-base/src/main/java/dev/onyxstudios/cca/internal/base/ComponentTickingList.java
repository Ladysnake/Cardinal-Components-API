/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2020 OnyxStudios
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

import dev.onyxstudios.cca.api.v3.component.TickingComponent;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.function.Predicate;

public final class ComponentTickingList<T> {
    /**
     * Default initial capacity.
     */
    private static final int DEFAULT_CAPACITY = 10;
    /**
     * Shared empty array instance used for default sized empty instances.
     */
    private static final WeakReference<?>[] EMPTY_ELEMENTDATA = {};

    private final Predicate<T> expirationTest;
    private WeakReference<?>[] elementData = EMPTY_ELEMENTDATA;
    private int size = 0;
    private final Deque<Entry<T>> pending = new ArrayDeque<>();
    private boolean ticking;

    public ComponentTickingList(Predicate<T> expirationTest) {
        this.expirationTest = expirationTest;
    }

    public void add(T anchor, TickingComponent component) {
        Entry<T> e = new Entry<>(anchor, component);
        if (this.ticking) {
            this.pending.offer(e);
        } else {
            this.add(e, this.size, this.elementData);
        }
    }

    public void tick() {
        final WeakReference<?>[] entries = this.elementData;
        final Predicate<T> test = this.expirationTest;
        final int end = this.size;

        this.ticking = true;

        int i = 0;
        // Optimize for initial run of survivors
        while (i < end && tick(test, elementAt(entries, i))) {
            i++;
        }

        if (i < end) {
            // overwrite the current entry, it has already been tested
            entries[i] = entries[i + 1];

            int writeCursor = ++i;

            for (; i < end; i++) {
                if (tick(test, elementAt(entries, i))) {
                    entries[writeCursor++] = entries[i];
                }
            }

            this.size = writeCursor;
            eraseTail(entries, writeCursor, end);
        }

        this.ticking = false;

        // Add pending entries
        for (Entry<T> e = this.pending.poll(); e != null; e = this.pending.poll()) {
            this.add(e, this.size, this.elementData);
        }
    }

    private void add(Entry<T> entry, int size, WeakReference<?>[] elementData) {
        if (size == elementData.length)
            elementData = this.grow(size + 1);
        elementData[size] = new WeakReference<>(entry);
        this.size = size + 1;
    }

    private WeakReference<?>[] grow(int minCapacity) {
        int oldCapacity = this.elementData.length;
        if (oldCapacity > 0) {
            int newCapacity = Math.max(oldCapacity + oldCapacity >> 1, minCapacity);
            return this.elementData = Arrays.copyOf(this.elementData, newCapacity);
        } else {
            return this.elementData = new WeakReference<?>[Math.max(DEFAULT_CAPACITY, minCapacity)];
        }
    }

    /** Erases the gap from newSize to size. */
    private static void eraseTail(Object[] es, int newSize, int curSize) {
        for (int i = newSize; i < curSize; i++) {
            es[i] = null;
        }
    }

    private static <T> boolean tick(Predicate<T> filter, WeakReference<Entry<T>> ref) {
        Entry<T> e = ref.get();
        if (e != null && !filter.test(e.anchor)) {
            e.component.tick();
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static <T> WeakReference<Entry<T>> elementAt(WeakReference<?>[] es, int index) {
        return (WeakReference<Entry<T>>) es[index];
    }

    private static final class Entry<T> {
        private final T anchor;
        private final TickingComponent component;

        private Entry(T anchor, TickingComponent component) {
            this.anchor = anchor;
            this.component = component;
        }
    }
}
