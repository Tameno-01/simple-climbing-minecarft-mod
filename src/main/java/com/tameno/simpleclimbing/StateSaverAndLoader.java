// Thanks to fabric wiki for this code (has been heavily modified)

package com.tameno.simpleclimbing;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

public class StateSaverAndLoader extends PersistentState {

    public HashMap<UUID, PlayerData> players = new HashMap<UUID, PlayerData>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((UUID uuid, PlayerData playerData) -> {
            playersNbt.put(uuid.toString(), playerData.toNbt());
        });
        nbt.put("players", playersNbt);
        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData(playersNbt.getCompound(key));
            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });
        return state;
    }

    public static StateSaverAndLoader createNew() {
        return new StateSaverAndLoader();
    }

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        // (Note: arbitrary choice to use 'World.OVERWORLD' instead of 'World.END' or 'World.NETHER'.  Any work)
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        // The first time the following 'getOrCreate' function is called, it creates a brand new 'StateSaverAndLoader' and
        // stores it inside the 'PersistentStateManager'. The subsequent calls to 'getOrCreate' pass in the saved
        // 'StateSaverAndLoader' NBT on disk to our function 'StateSaverAndLoader::createFromNbt'.

        return persistentStateManager.getOrCreate(
                StateSaverAndLoader::createFromNbt,
                StateSaverAndLoader::createNew,
                SimpleClimbing.MOD_ID
        );
    }

    public PlayerData.ClimbAbility getPlayerClimbingAbility(UUID uuid) {
        addNewPlayerIfNecessary(uuid);
        return players.get(uuid).canClimb;
    }

    public void setPlayerClimbingAbility(UUID uuid, PlayerData.ClimbAbility climbAbility) {
        addNewPlayerIfNecessary(uuid);
        players.get(uuid).canClimb = climbAbility;
        markDirty();
    }

    public void addNewPlayerIfNecessary(UUID uuid) {
        if (!players.containsKey(uuid)) {
            players.put(uuid, new PlayerData());
        }
    }
}
