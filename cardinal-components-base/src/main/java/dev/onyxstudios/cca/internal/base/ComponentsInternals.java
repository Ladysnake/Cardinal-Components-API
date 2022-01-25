/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2022 OnyxStudios
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

import dev.onyxstudios.cca.internal.base.asm.StaticComponentLoadingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public final class ComponentsInternals {
    public static final Logger LOGGER = LogManager.getLogger("Cardinal Components API");

    @Nonnull
    public static <R> R createFactory(Class<R> factoryClass) {
        try {
            Constructor<?>[] constructors = factoryClass.getConstructors();
            if (constructors.length != 1) {
                throw new IllegalArgumentException("Expected 1 constructor declaration in " + factoryClass + ", got " + Arrays.toString(constructors));
            }
            @SuppressWarnings("unchecked") R ret =
                (R) constructors[0].newInstance();
            return ret;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new StaticComponentLoadingException("Failed to instantiate generated component factory", e);
        }
    }
}
