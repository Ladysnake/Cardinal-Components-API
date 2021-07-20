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
package dev.onyxstudios.cca.internal.base;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.base.asm.CcaAsmHelper;
import dev.onyxstudios.cca.internal.base.asm.QualifiedComponentFactory;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentLoadingException;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public abstract class GenericContainerBuilder<I, R> {
    private static final AtomicInteger nextId = new AtomicInteger();

    private boolean built;
    private final Map<ComponentKey<?>, QualifiedComponentFactory<I>> factories = new LinkedHashMap<>();

    @ApiStatus.Experimental
    public void checkDuplicate(ComponentKey<?> key, Function<I, String> msgFactory) {
        if (this.factories.containsKey(key)) {
            throw new StaticComponentLoadingException(msgFactory.apply(this.factories.get(key).factory()));
        }
    }

    @Contract(mutates = "this")
    protected <C extends Component> void addComponent(ComponentKey<? super C> key, Class<C> implClass, I factory) {
        this.factories.put(key, new QualifiedComponentFactory<>(factory, implClass, Set.of()));
    }

    protected R build(@Nullable String factoryNameSuffix, R emptyFactory, Class<? super I> componentFactoryClass, Class<? super R> containerFactoryType, List<Class<?>> argClasses) {
        if (this.built) {
            throw new IllegalStateException("Cannot build more than one container factory with the same builder");
        }

        try {
            this.built = true;

            if (this.factories.isEmpty()) {
                return emptyFactory;
            }

            String implNameSuffix = factoryNameSuffix != null ? factoryNameSuffix : Integer.toString(nextId.getAndIncrement());
            Class<? extends ComponentContainer> containerClass = CcaAsmHelper.spinComponentContainer(
                componentFactoryClass, this.factories, implNameSuffix
            );
            Class<? extends R> factoryClass = StaticComponentPluginBase.spinContainerFactory(
                implNameSuffix, containerFactoryType, containerClass, argClasses
            );
            return ComponentsInternals.createFactory(factoryClass);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static final class SimpleImpl<I, R> extends GenericContainerBuilder<I, R> {
        @Override
        public <C extends Component> void addComponent(ComponentKey<? super C> key, Class<C> implClass, I factory) {
            super.addComponent(key, implClass, factory);
        }

        @Override
        public R build(@Nullable String factoryNameSuffix, R emptyFactory, Class<? super I> componentFactoryClass, Class<? super R> containerFactoryType, List<Class<?>> argClasses) {
            return super.build(factoryNameSuffix, emptyFactory, componentFactoryClass, containerFactoryType, argClasses);
        }
    }
}
