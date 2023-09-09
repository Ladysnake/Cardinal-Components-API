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

import kotlinx.serialization.Serializable
import kotlinx.serialization.minecraft.Nbt

/**
 * `@AutoSerialize` is used to signal that the annotated property should be serialized using kotlinx.serialization
 *
 * This annotation has no effect if used outside of a class implementing [AutoSerializedKomponent] and/or [AutoSyncedKomponent].
 *
 * Default serializable types include all those from [Nbt.serializersModule].
 * Custom data structures should be made serializable by applying the kotlinx.serialization gradle plugin and annotating them
 * with [@Serializable][Serializable].
 *
 * @see AutoSerializedKomponent
 */
@Target(AnnotationTarget.PROPERTY)
annotation class AutoSerialize(
    /**
     * The serialized name for this property
     *
     * If left to the default, will be the property name as declared in the source code
     */
    val key: String = "",
    /**
     * If set to `true`, enables automatic sync
     *
     * The value of this property must be the same on server and clients, otherwise there will be network errors.
     *
     * This property has no effect if the owning class does not implement [AutoSyncedKomponent].
     *
     * @see AutoSyncedKomponent
     */
    val sync: Boolean = false,
)
