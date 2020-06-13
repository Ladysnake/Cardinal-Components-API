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
package nerdhub.cardinal.componentstest.vita;

import dev.onyxstudios.cca.api.v3.util.PlayerComponent;
import nerdhub.cardinal.components.api.util.sync.EntitySyncedComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * A Vita component attached to players, and automatically synchronized with their owner
 */
public class PlayerVita extends EntityVita implements EntitySyncedComponent, PlayerComponent<BaseVita> {

    public PlayerVita(PlayerEntity owner) {
        super(owner, 0);
    }

    @Override
    public void sync() {
        if (!this.getEntity().world.isClient) {
            // We only sync with the holder, not with everyone around
            this.syncWith((ServerPlayerEntity) this.getEntity());
        }
    }

    @Override
    public void setVitality(int value) {
        super.setVitality(value);
        this.sync();
    }

    @Override
    public LivingEntity getEntity() {
        return this.owner;
    }

    @Override
    public boolean shouldCopyForRespawn(boolean lossless, boolean keepInventory) {
        return lossless || keepInventory;
    }

    @Override
    public void copyForRespawn(BaseVita original, boolean lossless, boolean keepInventory) {
        PlayerComponent.super.copyForRespawn(original, lossless, keepInventory);
        if (!lossless && !keepInventory) {
            this.vitality -= 5;
        }
    }
}
