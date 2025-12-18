package org.ladysnake.cca.test.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.ladysnake.cca.test.base.Energy;

public class PlayerEnergy {
    public static Energy onServerTick(Energy energy, PlayerEntity player) {
        return energy;
    }

    public static Energy onClientTick(Energy energy, PlayerEntity player) {
        player.sendMessage(Text.literal("hi!!"), false);
        return energy;
    }

    public static Energy onServerLoad(Energy energy, PlayerEntity player) {
        return energy;
    }

    public static Energy onClientLoad(Energy energy, PlayerEntity player) {
        return energy;
    }
}
