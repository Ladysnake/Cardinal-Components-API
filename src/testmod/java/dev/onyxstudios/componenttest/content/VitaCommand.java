/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2022 OnyxStudios
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
package dev.onyxstudios.componenttest.content;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.onyxstudios.cca.test.base.Vita;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.chunk.Chunk;

public final class VitaCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("ccatest")
            .then(CommandManager.literal("set")
                .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                    .executes(context -> {
                        Vita.get(context.getSource().getWorld()).setVitality(IntegerArgumentType.getInteger(context, "amount"));
                        context.getSource().sendFeedback(Text.of("success!"), false);
                        return 1;
                    })
                    .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                        .executes(context -> {
                            Chunk chunk = context.getSource().getWorld().getChunk(BlockPosArgumentType.getBlockPos(context, "pos"));
                            Vita.get(chunk).setVitality(IntegerArgumentType.getInteger(context, "amount"));
                            chunk.setShouldSave(true);
                            context.getSource().sendFeedback(Text.of("success!"), false);
                            return 1;
                        })
                    )
                )
            )
            .then(CommandManager.literal("get")
                .executes(context -> {
                    // Dedicated servers cannot handle translations
                    context.getSource().sendFeedback(Text.of("World vitality: " + Vita.get(context.getSource().getWorld()).getVitality()), false);
                    return 1;
                })
                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                    .executes(context -> {
                        context.getSource().sendFeedback(Text.of("Chunk vitality: " + Vita.get(context.getSource().getWorld().getChunk(BlockPosArgumentType.getBlockPos(context, "pos"))).getVitality()), false);
                        return 1;
                    }))
            )
        );
    }
}
