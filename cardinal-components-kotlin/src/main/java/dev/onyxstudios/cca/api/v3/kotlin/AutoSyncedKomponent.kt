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
package dev.onyxstudios.cca.api.v3.kotlin

import dev.onyxstudios.cca.api.v3.component.ComponentAccess
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import dev.onyxstudios.cca.impl.v3.kotlin.KomponentSerializer
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import kotlin.properties.Delegates
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.full.findAnnotation

/**
 * @see AutoSerialize
 */
interface AutoSyncedKomponent : AutoSerializedKomponent, AutoSyncedComponent {
    /**
     * Returns a property delegate for a read/write property that triggers [ComponentKey.sync] when changed
     * @param owner the object to which this component is attached
     * @param initialValue the initial value of the property
     */
    // TODO consider using context receivers to ensure compile-time safety (self-types parameters are too burdensome)
    // context(C) fun <C> ComponentKey<out C>.autoSync
    fun <V> ComponentKey<*>.autoSync(owner: ComponentAccess, initialValue: V): PropertyDelegateProvider<AutoSyncedKomponent, ReadWriteProperty<Any?, V>> {
        require(this.componentClass.isInstance(this@AutoSyncedKomponent)) {
            "Mismatched keys: $this used to sync ${this@AutoSyncedKomponent}"
        }

        return PropertyDelegateProvider { _, prop ->
            check(prop.findAnnotation<AutoSerialize>()?.sync == true) {
                "$prop is not declared as a synced serialized value - missing @AutoSerialize(sync = true)"
            }

            Delegates.observable(initialValue) { _, _, _ ->
                sync(owner)
            }
        }
    }

    override fun writeSyncPacket(buf: PacketByteBuf, recipient: ServerPlayerEntity) {
        KomponentSerializer.get(this::class).writeToPacket(this, buf)
    }

    override fun applySyncPacket(buf: PacketByteBuf) {
        KomponentSerializer.get(this::class).readFromPacket(this, buf)
    }
}
