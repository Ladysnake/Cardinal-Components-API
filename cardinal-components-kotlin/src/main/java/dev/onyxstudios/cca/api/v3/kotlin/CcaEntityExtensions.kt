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

import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.ComponentFactory
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import java.util.function.Predicate
import kotlin.reflect.KClass

inline fun <reified E : Entity, C : Component, reified I : C> EntityComponentFactoryRegistry.register(
    key: ComponentKey<C>,
    factory: ComponentFactory<E, I>
) {
    registerFor(E::class, key, factory)
}

inline fun <E : Entity, C : Component, reified I : C> EntityComponentFactoryRegistry.registerFor(
    target: KClass<E>,
    key: ComponentKey<C>,
    factory: ComponentFactory<E, I>
) {
    beginRegistration(target.java, key).impl(I::class.java).end(factory)
}

inline fun <reified E : Entity, C : Component, reified I : C> EntityComponentFactoryRegistry.register(
    key: ComponentKey<C>,
    factory: ComponentFactory<E, I>,
    builder: KRegistration<E, C, I>.() -> Unit
) {
    val reg: KRegistration<E, C, I> = KRegistration<E, C, I>(beginRegistration(E::class.java, key).impl(I::class.java))
    reg.apply(builder)
    reg.reg.end(factory)
}

class KRegistration<E : Entity, C : Component, I : C>(@PublishedApi internal val reg : EntityComponentFactoryRegistry.Registration<I, E>) {
    fun orderAfter(dependency: ComponentKey<*>) {
        reg.after(dependency)
    }

    fun filter(test: Predicate<Class<out E>>) {
        reg.filter(test)
    }
}

fun <E : PlayerEntity, I : Component> KRegistration<in E, *, I>.respawnStrategy(strategy: RespawnCopyStrategy<in I>) {
    reg.respawnStrategy(strategy)
}
