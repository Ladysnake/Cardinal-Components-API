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
package dev.onyxstudios.cca.impl.v3.kotlin

import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.kotlin.AutoSerialize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.minecraft.Buf
import kotlinx.serialization.minecraft.Nbt
import kotlinx.serialization.serializer
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

internal class KomponentSerializer(private val fields: List<SerializedField<*>>) {
    companion object {
        private val registry: MutableMap<KClass<out Component>, KomponentSerializer> = ConcurrentHashMap()

        fun get(selfType: KClass<out Component>): KomponentSerializer {
            return registry.computeIfAbsent(selfType) { kclass ->
                val serializedFields = kclass.memberProperties.mapNotNull(::parse)
                KomponentSerializer(serializedFields)
            }
        }

        private fun <V> parse(prop: KProperty1<out Component, V>): SerializedField<V>? {
            val annotation = prop.findAnnotation<AutoSerialize>()

            return if (annotation == null) {
                null
            } else {
                check(prop is KMutableProperty1<out Component, V>) {
                    "$prop is annotated with @SerializedValue but has no setter"
                }

                prop.isAccessible = true
                val key = annotation.key.ifEmpty { prop.name }
                val synced = annotation.sync
                @Suppress("UNCHECKED_CAST")
                SerializedField(prop as KMutableProperty1<in Component, V>, key, synced)
            }
        }
    }

    fun readFromNbt(instance: Component, tag: NbtCompound) {
        for (field in fields) {
            field.readFromNbt(instance, tag)
        }
    }

    fun writeToNbt(instance: Component, tag: NbtCompound) {
        for (field in fields) {
            field.writeToNbt(instance, tag)
        }
    }

    fun readFromPacket(instance: Component, buf: PacketByteBuf) {
        for (field in fields) {
            if (field.synced) {
                field.readFromPacket(instance, buf)
            }
        }
    }

    fun writeToPacket(instance: Component, buf: PacketByteBuf) {
        for (field in fields) {
            if (field.synced) {
                field.writeToPacket(instance, buf)
            }
        }
    }

    class SerializedField<V>(
        private val property: KMutableProperty1<in Component, V>,
        private val key: String,
        val synced: Boolean
    ) {
        @Suppress("UNCHECKED_CAST")
        private val serializer = Nbt.serializersModule.serializer(property.returnType) as KSerializer<V>

        fun readFromNbt(instance: Component, tag: NbtCompound) {
            property.set(instance, Nbt.decodeFromNbt(serializer, tag.get(key) ?: return))
        }

        fun writeToNbt(instance: Component, tag: NbtCompound) {
            tag.put(key, Nbt.encodeToNbt(serializer, property.get(instance)))
        }

        fun readFromPacket(instance: Component, buf: PacketByteBuf) {
            property.set(instance, Buf.decodeFromByteBuf(serializer, buf))
        }

        fun writeToPacket(instance: Component, buf: PacketByteBuf) {
            Buf.encodeToByteBuf(serializer, property.get(instance), buf)
        }
    }
}
