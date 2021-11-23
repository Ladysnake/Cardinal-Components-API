/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2021 OnyxStudios
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
package dev.onyxstudios.componenttest.tests;

import dev.onyxstudios.componenttest.content.CardinalComponentsTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface CardinalGameTest extends FabricGameTest {
    @Override
    default void invokeTestMethod(TestContext context, Method method) {
        try {
            this.setUp();
            if (method.getParameterCount() > 1) {
                method.invoke(this, context);
            } else {
                method.invoke(this);
                context.complete();
            }
            this.tearDown();
        } catch (Throwable e) {
            String message;
            if (e instanceof InvocationTargetException) {
                message = e.getMessage() + ": " + e.getCause().getMessage();
                e = e.getCause();
            } else {
                message = e.getMessage();
            }
            CardinalComponentsTest.LOGGER.error("Failed test", e);
            throw new GameTestException(message);
        }
    }

    default void setUp() {

    }

    default void tearDown() {

    }
}
