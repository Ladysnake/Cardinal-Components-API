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
@file:Suppress("FunctionName")

package dev.onyxstudios.cca.test.kotlin

import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.test.kotlin.TestKomponent.Companion.testKomponent
import io.github.ladysnake.elmendorf.ByteBufChecker
import io.github.ladysnake.elmendorf.CheckedConnection
import io.github.ladysnake.elmendorf.GameTestUtil.assertTrue
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.test.GameTest
import net.minecraft.test.TestContext
import java.lang.reflect.Method
import java.util.function.Consumer

class CcaKotlinTestSuite : FabricGameTest {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    fun TestContext.`TestKomponentImpl should serialize and sync correctly`() {
        val p = spawnServerPlayer(1.0, 0.0, 1.0)

        // ServerLoadAwareComponent check
        assertTrue("mana should be set to 10 after joining the world", p.testKomponent.mana == 10)

        // AutoSyncedKomponent check
        verifyConnection(p) {
            it.hasSentEntityComponentUpdate(p, TestKomponent.KEY) {
                checkByte(1)    // not null mark
                checkInt(10)
                noMoreData()
            }
        }

        // AutoSerializedKomponent check
        val expected = NbtCompound().apply {
            putInt("mana", 10)
            putString("x", "test")
        }
        val actual = NbtCompound().apply(p.testKomponent::writeToNbt)
        assertTrue("TestKomponent should serialize correctly", expected == actual)

        actual.putInt("mana", 3)
        p.testKomponent.readFromNbt(actual)
        assertTrue("TestKomponent should deserialize correctly", p.testKomponent.mana == 3)

        // AutoSyncedKomponent#autoSync check
        verifyConnection(p) {
            it.hasSentEntityComponentUpdate(p, TestKomponent.KEY) {
                checkByte(1)    // not null mark
                checkInt(3)
                noMoreData()
            }
        }

        complete()
    }

    companion object {
        // Need to do a funny because classpath issues, something something circular dependencies probably
        private val sentEntityComponentUpdate: Method = CheckedConnection::class.java.getMethod("sentEntityComponentUpdate", Entity::class.java, ComponentKey::class.java, Consumer::class.java)

        fun CheckedConnection.hasSentEntityComponentUpdate(e: Entity, key: ComponentKey<*>, checker: ByteBufChecker.() -> Unit) {
            sentEntityComponentUpdate(this, e, key, Consumer(checker))
        }
    }
}
