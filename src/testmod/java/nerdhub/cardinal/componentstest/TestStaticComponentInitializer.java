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
package nerdhub.cardinal.componentstest;

import com.google.common.reflect.TypeToken;
import dev.onyxstudios.cca.api.v3.component.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.component.chunk.StaticChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.component.entity.StaticEntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.component.item.StaticItemComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.component.level.StaticLevelComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.util.GenericComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.component.util.StaticGenericComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.world.StaticWorldComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.world.WorldComponentFactoryRegistry;
import nerdhub.cardinal.componentstest.vita.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.function.BiFunction;

public final class TestStaticComponentInitializer implements
    StaticEntityComponentInitializer,
        StaticChunkComponentInitializer,
    StaticLevelComponentInitializer,
        StaticWorldComponentInitializer,
    StaticGenericComponentInitializer,
    StaticItemComponentInitializer {

    public static final Identifier VITA_ID = new Identifier("componenttest:vita");
    public static final Identifier ALT_VITA_ID = new Identifier("componenttest:alt_vita");
    public static final Identifier CUSTOM_PROVIDER_1 = new Identifier("componenttest:custom/1");
    public static final Identifier CUSTOM_PROVIDER_2 = new Identifier("componenttest:custom/2");
    public static final Identifier CUSTOM_PROVIDER_3 = new Identifier("componenttest:custom/3");
    public static final TypeToken<BiFunction<UUID, PlayerEntity, BaseVita>> CUSTOM_FACTORY_TYPE = new TypeToken<BiFunction<UUID, PlayerEntity, BaseVita>>() {};

    private static BaseVita createForEntity(LivingEntity e) {
        return new BaseVita((int) (Math.random() * 10));
    }

    @Override
    public Collection<Identifier> getSupportedComponentTypes() {
        return Arrays.asList(VITA_ID, ALT_VITA_ID);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.register(Vita.ALT_TYPE, HostileEntity.class, e -> new BaseVita());
        registry.register(Vita.TYPE, LivingEntity.class, TestStaticComponentInitializer::createForEntity);
        registry.register(Vita.TYPE, PlayerEntity.class, PlayerVita::new);
        registry.register(Vita.TYPE, VitalityZombieEntity.class, VitalityZombieEntity::createVitaComponent);
        registry.register(Vita.TYPE, PhantomEntity.class, p -> {
            return p.getType() == EntityType.PHANTOM ? null : createForEntity(p); // fuck vanilla phantoms specifically
        });
    }

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(Vita.TYPE, ChunkVita::new);
    }

    @Override
    public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
        registry.register(Vita.TYPE, properties -> new AmbientVita.LevelVita());
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(Vita.TYPE, AmbientVita.WorldVita::new);
    }

    @Override
    public void registerGenericComponentFactories(GenericComponentFactoryRegistry registry) {
        BiFunction<UUID, PlayerEntity, BaseVita> createForThirdParty = (uuid, p) -> new BaseVita();
        registry.register(Vita.TYPE, CUSTOM_PROVIDER_1, CUSTOM_FACTORY_TYPE, createForThirdParty);
        registry.register(Vita.TYPE, CUSTOM_PROVIDER_2, CUSTOM_FACTORY_TYPE, createForThirdParty);
        registry.register(Vita.TYPE, CUSTOM_PROVIDER_3, CUSTOM_FACTORY_TYPE, createForThirdParty);
    }

    @Override
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
        registry.register(Vita.TYPE, null, (item, stack) -> new BaseVita(stack.getCount()));
        registry.register(Vita.TYPE, CardinalComponentsTest.VITA_STICK_ID, stack -> new BaseVita());
    }

    @Override
    public void finalizeStaticBootstrap() {
        CardinalComponentsTest.LOGGER.info("CCA Bootstrap complete!");
        CardinalComponentsTest.TestCallback.EVENT.register((uuid, p, components) -> components.put(Vita.ALT_TYPE, new BaseVita()));
    }

}
