/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2025 Ladysnake
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
package org.ladysnake.cca.test.base;

import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface CardinalGameTest {
    Logger LOGGER = LogManager.getLogger();

    default void invokeTestMethod(TestContext context, Method method) {
        try {
            this.setUp();
            if (method.getParameterCount() == 0) {
                method.invoke(this);
                context.complete();
            } else {
                method.invoke(this, context);
            }
        } catch (Throwable e) {
            String message;
            Throwable cause;
            if (e instanceof InvocationTargetException) {
                message = e.getMessage() + ": " + e.getCause().getMessage();
                cause = e.getCause();
            } else {
                message = e.getMessage();
                cause = e;
            }
            LOGGER.error("Failed test", cause);
            throw context.createError(Text.literal(message));
        } finally {
            this.tearDown(context);
        }
    }

    default void setUp() {

    }

    default void tearDown(TestContext ctx) {

    }
}
