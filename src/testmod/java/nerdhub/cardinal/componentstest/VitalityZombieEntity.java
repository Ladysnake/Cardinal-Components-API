/*
 * Cardinal-Components-API
 * Copyright (C) 2019 GlassPane
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

import nerdhub.cardinal.components.api.component.ComponentContainer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;

public class VitalityZombieEntity extends ZombieEntity {
    public VitalityZombieEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.world.isClient) {
            this.world.addParticle(ParticleTypes.DRAGON_BREATH, this.x, this.y + 0.3D, this.z, this.random.nextGaussian() * 0.05D, this.random.nextGaussian() * 0.05D, this.random.nextGaussian() * 0.05D);
        }
    }

    protected void initComponents(ComponentContainer components) {
        components.put(CardinalComponentsTest.VITA, new EntityVita(this, 20));
    }
}
