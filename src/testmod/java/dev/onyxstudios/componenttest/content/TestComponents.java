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
package dev.onyxstudios.componenttest.content;

import dev.onyxstudios.cca.api.v3.block.BlockComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.block.BlockComponentInitializer;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.item.ItemComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.item.ItemComponentInitializer;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.scoreboard.ScoreboardComponentInitializer;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import dev.onyxstudios.componenttest.content.vita.AmbientVita;
import dev.onyxstudios.cca.test.base.BaseVita;
import dev.onyxstudios.componenttest.content.vita.ChunkVita;
import dev.onyxstudios.componenttest.content.vita.ItemVita;
import dev.onyxstudios.componenttest.content.vita.SyncedVita;
import dev.onyxstudios.componenttest.content.vita.TeamVita;
import dev.onyxstudios.cca.test.base.Vita;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public final class TestComponents implements
    EntityComponentInitializer,
    ChunkComponentInitializer,
    BlockComponentInitializer,
    LevelComponentInitializer,
    WorldComponentInitializer,
    ItemComponentInitializer,
    ScoreboardComponentInitializer {

    public static final Identifier CUSTOM_PROVIDER_1 = new Identifier("componenttest:custom/1");
    public static final Identifier CUSTOM_PROVIDER_2 = new Identifier("componenttest:custom/2");

    public static final ComponentKey<Vita> ALT_VITA = ComponentRegistryV3.INSTANCE.getOrCreate(TestStaticComponentInitializer.ALT_VITA_ID, Vita.class);

    private static BaseVita createForEntity(LivingEntity e) {
        return new BaseVita((int) (Math.random() * 10));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(HostileEntity.class, ALT_VITA).after(Vita.KEY).end(e -> new BaseVita());
        registry.registerFor(LivingEntity.class, Vita.KEY, TestComponents::createForEntity);
        registry.registerFor(VitalityZombieEntity.class, Vita.KEY, VitalityZombieEntity::createVitaComponent);
        registry.beginRegistration(LivingEntity.class, ALT_VITA).filter(CowEntity.class::isAssignableFrom).end(TestComponents::createForEntity);
    }

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry) {
        registry.register(Vita.KEY, ChunkVita::new);
    }

    @Override
    public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {
        registry.registerFor(EndGatewayBlockEntity.class, VitaCompound.KEY, VitaCompound::new);
        registry.beginRegistration(EndPortalBlockEntity.class, ALT_VITA).after(Vita.KEY).impl(SyncedVita.class).end(SyncedVita::new);
        registry.registerFor(EndPortalBlockEntity.class, Vita.KEY, SyncedVita::new);
    }

    @Override
    public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
        registry.register(Vita.KEY, properties -> new AmbientVita.LevelVita());
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(Vita.KEY, AmbientVita.WorldVita.class, AmbientVita.WorldVita::new);
    }

    @Override
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry) {
        // this first line adds data to every stack, which is a fairly bad idea for several reasons
        // it also initializes the component with volatile data (stack count), which is an even worse idea and will cause desync
        registry.register(i -> true, ALT_VITA, (stack) -> {
            ItemVita ret = new ItemVita(stack);
            ret.setVitality(stack.getItem() == Items.DIAMOND_CHESTPLATE ? 3 : stack.getCount());
            return ret;
        });
        registry.register(CardinalComponentsTest.VITALITY_STICK, Vita.KEY, ItemVita::new);
    }

    @Override
    public void registerScoreboardComponentFactories(ScoreboardComponentFactoryRegistry registry) {
        registry.registerScoreboardComponent(Vita.KEY, (scoreboard, server) -> new BaseVita());
        registry.registerTeamComponent(Vita.KEY, (t, sc, s) -> new TeamVita(t));
    }
}
