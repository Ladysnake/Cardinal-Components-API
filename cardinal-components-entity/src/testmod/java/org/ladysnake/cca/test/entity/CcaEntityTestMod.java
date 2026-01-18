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
package org.ladysnake.cca.test.entity;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.test.base.BaseVita;
import org.ladysnake.cca.test.base.LoadAwareTestComponent;
import org.ladysnake.cca.test.base.Vita;

public class CcaEntityTestMod implements ModInitializer, EntityComponentInitializer {
    public static final ResourceKey<EntityType<?>> TEST_ENTITY_ID = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("cca-entity-test", "test"));
    public static final EntityType<TestEntity> TEST_ENTITY = EntityType.Builder.of(TestEntity::new, MobCategory.MISC).build(TEST_ENTITY_ID);
    public static final int NATURAL_VITA_CEILING = 10;
    public static final int CAMEL_BASE_VITA = 50;

    public static BaseVita createForEntity(LivingEntity e) {
        return new BaseVita((int) (Math.random() * NATURAL_VITA_CEILING));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(LivingEntity.class, Vita.KEY, CcaEntityTestMod::createForEntity);
        registry.beginRegistration(Player.class, Vita.KEY).impl(PlayerVita.class).end(PlayerVita::new);
        registry.beginRegistration(Camel.class, Vita.KEY).impl(EntityVita.class).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(owner -> new EntityVita(owner, CAMEL_BASE_VITA));
        registry.beginRegistration(Shulker.class, LoadAwareTestComponent.KEY).impl(LoadAwareTestComponent.class).end(e -> new LoadAwareTestComponent());
    }

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.ENTITY_TYPE, TEST_ENTITY_ID, TEST_ENTITY);
    }
}
