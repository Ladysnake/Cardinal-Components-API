/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2024 Ladysnake
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
package org.ladysnake.cca.internal.item;

import net.fabricmc.api.ModInitializer;
import net.minecraft.component.ComponentType;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.item.ItemComponentInitializer;
import org.ladysnake.cca.api.v3.item.ItemComponentMigrationRegistry;
import org.ladysnake.cca.internal.base.ComponentsInternals;
import org.ladysnake.cca.internal.base.asm.StaticComponentPluginBase;

import java.util.HashMap;
import java.util.Map;

public final class StaticItemComponentPlugin implements ItemComponentMigrationRegistry, ModInitializer {
    public static final StaticItemComponentPlugin INSTANCE = new StaticItemComponentPlugin();

    private StaticItemComponentPlugin() {}

    private final Map<Identifier, ComponentType<?>> migrations = new HashMap<>();

    @Override
    public void registerMigration(Identifier oldComponentId, ComponentType<?> mcComponentType) {
        if (this.migrations.put(oldComponentId, mcComponentType) != null) {
            ComponentsInternals.LOGGER.warn("[Cardinal-Components-API] Overwriting component migration for {}", oldComponentId);
        }
    }

    public void migrate(ItemStackComponentizationFix.StackData data) {
        for (Map.Entry<Identifier, ComponentType<?>> entry : migrations.entrySet()) {
            String oldComponentId = entry.getKey().toString();
            String mcComponentId = Registries.DATA_COMPONENT_TYPE.getKey(entry.getValue()).orElseThrow(
                () -> new IllegalStateException("Registered migration for component " + oldComponentId + " towards unregistered item component type " + entry.getValue())
            ).getValue().toString();
            data.moveToComponent(oldComponentId, mcComponentId);
        }
    }

    @Override
    public void onInitialize() {
        StaticComponentPluginBase.processInitializers(
            StaticComponentPluginBase.getComponentEntrypoints("cardinal-components-item", ItemComponentInitializer.class),
            initializer -> initializer.registerItemComponentMigrations(this)
        );
    }
}
