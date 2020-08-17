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
package dev.onyxstudios.cca.mixin.scoreboard;

import dev.onyxstudios.cca.api.v3.component.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import dev.onyxstudios.cca.internal.base.InternalComponentProvider;
import dev.onyxstudios.cca.internal.scoreboard.ComponentsScoreboardNetworking;
import dev.onyxstudios.cca.internal.scoreboard.StaticScoreboardComponentPlugin;
import nerdhub.cardinal.components.api.component.Component;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Lazy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;

@Mixin(Scoreboard.class)
public abstract class MixinScoreboard implements InternalComponentProvider {
    @Unique
    private static final Lazy<DynamicContainerFactory<Scoreboard, Component>> componentsContainerFactory
        = new Lazy<>(() -> ComponentsInternals.createFactory(StaticScoreboardComponentPlugin.INSTANCE.getContainerFactoryClass()));
    @Unique
    private ComponentContainer<Component> components;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initComponents(CallbackInfo ci) {
        this.components = componentsContainerFactory.get().create((Scoreboard) (Object) this);
    }

    @Nonnull
    @Override
    public ComponentContainer<?> getComponentContainer() {
        return this.components;
    }

    @Override
    public Iterator<ServerPlayerEntity> getRecipientsForComponentSync() {
        if (this instanceof ServerScoreboardAccessor) {
            return PlayerStream.all(((ServerScoreboardAccessor) this).getServer()).iterator();
        }
        return Collections.emptyIterator();
    }

    @Nullable
    @Override
    public <C extends AutoSyncedComponent> CustomPayloadS2CPacket toComponentPacket(PacketByteBuf buf, ComponentKey<? super C> key, C component, ServerPlayerEntity recipient) {
        buf.writeIdentifier(key.getId());
        component.writeToPacket(buf, recipient);
        return new CustomPayloadS2CPacket(ComponentsScoreboardNetworking.SCOREBOARD_PACKET_ID, buf);
    }

}
