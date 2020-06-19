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
package dev.onyxstudios.componenttest;

import dev.onyxstudios.cca.api.v3.component.StaticComponentInitializer;
import dev.onyxstudios.componenttest.vita.BaseVita;
import dev.onyxstudios.componenttest.vita.Vita;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;

public final class TestStaticComponentInitializer implements StaticComponentInitializer {

    // note: the actual ComponentKey must not be registered in this class' <clinit>, to avoid circular initialization
    public static final Identifier ALT_VITA_ID = new Identifier("componenttest:alt_vita");

    @Override
    public Collection<Identifier> getSupportedComponentTypes() {
        return Collections.singleton(ALT_VITA_ID);
    }

    @Override
    public void finalizeStaticBootstrap() {
        CardinalComponentsTest.LOGGER.info("CCA Bootstrap complete!");
        ComponentType<Vita> altVita = ComponentRegistry.INSTANCE.registerIfAbsent(ALT_VITA_ID, Vita.class);
        CardinalComponentsTest.TestCallback.EVENT.register((uuid, p, components) -> components.put(altVita, new BaseVita()));
    }

}
