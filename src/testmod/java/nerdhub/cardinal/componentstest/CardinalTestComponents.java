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

import nerdhub.cardinal.components.api.*;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.event.LevelComponentCallback;
import nerdhub.cardinal.components.api.event.WorldComponentCallback;
import nerdhub.cardinal.componentstest.vita.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.Chunk;

public final class CardinalTestComponents {
    public static final String VITA_ID = "componenttest:vita";
    public static final ComponentType<Vita> VITA = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier(VITA_ID), Vita.class)
        // the following will make all items unstackable, terrible for playing, great for testing
        .attach(WorldComponentCallback.EVENT, AmbientVita.WorldVita::new)
        .attach(LevelComponentCallback.EVENT, props -> new AmbientVita.LevelVita());

    @ItemComponentFactory(VITA_ID)
    public static Vita createForStack(ItemStack stack) {
        return new BaseVita(stack.getCount());
    }

    @ItemComponentFactory(value = VITA_ID, targets = CardinalComponentsTest.VITA_STICK_ID)
    public static Component createForStick() {
        return new BaseVita();
    }

    @ChunkComponentFactory(VITA_ID)
    public static Component createForChunk(Chunk chunk) {
        return new ChunkVita(chunk);
    }

    @EntityComponentFactory(VITA_ID)
    public static PlayerVita createForEntity(PlayerEntity player) {
        return new PlayerVita(player);
    }

    @EntityComponentFactory(VITA_ID)
    public static Component createForEntity(VitalityZombieEntity zombie) {
        return zombie.createVitaComponent();
    }

    @EntityComponentFactory(value = VITA_ID, target = LivingEntity.class)
    public static Vita createForEntity() {
        return new BaseVita((int)(Math.random() * 10));
    }
}
