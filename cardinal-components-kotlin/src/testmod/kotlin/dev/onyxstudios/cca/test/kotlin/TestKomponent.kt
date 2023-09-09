/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 OnyxStudios
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
package dev.onyxstudios.cca.test.kotlin

import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.load.ServerLoadAwareComponent
import dev.onyxstudios.cca.api.v3.kotlin.AutoSerialize
import dev.onyxstudios.cca.api.v3.kotlin.AutoSyncedKomponent
import dev.onyxstudios.cca.api.v3.kotlin.componentKey
import dev.onyxstudios.cca.api.v3.kotlin.getValue
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

interface TestKomponent : Component {
    val mana: Int

    companion object {
        val KEY: ComponentKey<TestKomponent> = componentKey<TestKomponent>(Identifier("cca-kotlin-test", "ktest"))
        val Entity.testKomponent by KEY
    }

    class Impl(owner: LivingEntity) : TestKomponent, ServerLoadAwareComponent, AutoSyncedKomponent {
        @AutoSerialize(sync = true)
        override var mana by KEY.autoSync(owner, -1)

        @AutoSerialize
        private var x = "test"

        override fun writeSyncPacket(buf: PacketByteBuf, recipient: ServerPlayerEntity) {
            super.writeSyncPacket(buf, recipient)
        }

        override fun loadServerside() {
            if (mana == -1) mana = 10
        }
    }
}
