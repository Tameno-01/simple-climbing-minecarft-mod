package com.tameno.simpleclimbing;

import net.minecraft.nbt.NbtCompound;

public class PlayerData {
    public enum ClimbAbility {
        DEFAULT,
        TRUE,
        FALSE,
    }

    public ClimbAbility canClimb = ClimbAbility.DEFAULT;

    public PlayerData() {}

    public PlayerData(NbtCompound nbt) {
        canClimb = ClimbAbility.values()[nbt.getInt("can_climb")];
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("can_climb", canClimb.ordinal());
        return nbt;
    }
}
