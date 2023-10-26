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
package org.ladysnake.componenttest.content;

import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.component.StaticComponentInitializer;
import org.ladysnake.cca.test.base.BaseVita;
import org.ladysnake.cca.test.base.Vita;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class TestStaticComponentInitializer implements StaticComponentInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Component Test Bootstrap");

    // note: the actual ComponentKey must not be registered in this class' <clinit>, to avoid circular initialization
    public static final Identifier ALT_VITA_ID = new Identifier("componenttest", "alt-vita");

    @Override
    public Collection<Identifier> getSupportedComponentKeys() {
        List<Identifier> ret = new ArrayList<>(Collections.singleton(ALT_VITA_ID));
        for (int i = 0; i < 128; i++) {
            ret.add(new Identifier("-.-", "-random/test." + i));
        }
        return ret;
    }

    @Override
    public void finalizeStaticBootstrap() {
        LOGGER.info("CCA Bootstrap complete!");
        LOGGER.info(ComponentRegistry.getOrCreate(ALT_VITA_ID, Vita.class));
        ComponentContainer.Factory.Builder<Void> builder = ComponentContainer.Factory.builder(Void.class);
        for (int i = 127; i >= 0; i--) {
            builder.component(ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("-.-", "-random/test." + i), Vita.class), v -> new BaseVita());
        }
        LOGGER.info(builder.build().createContainer(null));
    }
}
