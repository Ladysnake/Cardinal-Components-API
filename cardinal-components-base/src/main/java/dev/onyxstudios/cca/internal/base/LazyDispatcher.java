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

import org.jetbrains.annotations.ApiStatus;

public abstract class LazyDispatcher {
    private volatile boolean requiresInitialization = true;
    private volatile boolean loading = false;
    private final String likelyInitTrigger;

    protected LazyDispatcher(String likelyInitTrigger) {
        this.likelyInitTrigger = likelyInitTrigger;
    }

    public void ensureInitialized() {
        if (this.requiresInitialization) {
            synchronized (this) {
                if (this.requiresInitialization) {
                    if (this.loading) {
                        this.onCircularLoading();
                    } else {
                        this.loading = true;

                        try {
                            this.init();
                        } finally {
                            this.loading = false;
                        }

                        this.requiresInitialization = false;
                        this.postInit();
                    }
                }
            }
        }
    }

    protected void onCircularLoading() {
        throw new IllegalStateException("Circular loading issue, a mod is probably "  + this.likelyInitTrigger + " in the wrong place");
    }

    protected boolean requiresInitialization() {
        return this.requiresInitialization;
    }

    @ApiStatus.OverrideOnly
    protected abstract void init();

    public void checkLoading(Class<?> ownerClass, String methodName) {
        if (!this.loading) {
            throw new IllegalStateException(ownerClass.getSimpleName() + "#" + methodName + " called at the wrong time");
        }
    }

    protected void postInit() {
        // NO-OP
    }
}
