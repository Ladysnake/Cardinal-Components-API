/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2026 Ladysnake
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
package org.ladysnake.componenttest.content;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.ladysnake.cca.test.base.Vita;

public final class VitaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ccatest")
            .then(Commands.literal("set")
                .then(Commands.argument("amount", IntegerArgumentType.integer())
                    .executes(context -> {
                        Vita.get(context.getSource().getLevel()).setVitality(IntegerArgumentType.getInteger(context, "amount"));
                        context.getSource().sendSuccess(() -> Component.nullToEmpty("success!"), false);
                        return 1;
                    })
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> {
                            ChunkAccess chunk = context.getSource().getLevel().getChunk(BlockPosArgument.getBlockPos(context, "pos"));
                            Vita.get(chunk).setVitality(IntegerArgumentType.getInteger(context, "amount"));
                            chunk.markUnsaved();
                            context.getSource().sendSuccess(() -> Component.nullToEmpty("success!"), false);
                            return 1;
                        })
                    )
                )
            )
            .then(Commands.literal("get")
                .executes(context -> {
                    // Dedicated servers cannot handle translations
                    context.getSource().sendSuccess(() -> Component.nullToEmpty("World vitality: " + Vita.get(context.getSource().getLevel()).getVitality()), false);
                    return 1;
                })
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(context -> {
                        context.getSource().sendSuccess(() -> Component.nullToEmpty("Chunk vitality: " + Vita.get(context.getSource().getLevel().getChunk(BlockPosArgument.getBlockPos(context, "pos"))).getVitality()), false);
                        return 1;
                    }))
            )
        );
    }
}
