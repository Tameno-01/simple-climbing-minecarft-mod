package com.tameno.simpleclimbing;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleClimbing implements ModInitializer {
	public static final String MOD_ID = "simple-climbing";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final GameRules.Key<GameRules.BooleanRule> ENABLE_CLIMBING_BY_DEFAULT = GameRuleRegistry.register(
			"enableClimbingByDefault",
			GameRules.Category.PLAYER,
			GameRuleFactory.createBooleanRule(true)
	);

	public static Identifier SET_CLIMBING_ALLOWED = new Identifier(MOD_ID, "set_climbing_allowed");

	@Override
	public void onInitialize() {

		ServerPlayConnectionEvents.JOIN.register((
				ServerPlayNetworkHandler handler,
				PacketSender sender,
				MinecraftServer server
		) -> {

			updateAbilityToClimbForPlayer(handler.getPlayer(), server);

		});

	}

	public static void updateAbilityToClimbForPlayer(ServerPlayerEntity player, MinecraftServer server) {
		boolean canClimb = server.getGameRules().getBoolean(ENABLE_CLIMBING_BY_DEFAULT);
		PacketByteBuf buff = PacketByteBufs.create();
		buff.writeBoolean(canClimb);
		ServerPlayNetworking.send(player, SET_CLIMBING_ALLOWED, buff);
	}

	public static void updateAbilityToClimbForAllPlayers(MinecraftServer server) {
		for (ServerPlayerEntity player : PlayerLookup.all(server)) {
			updateAbilityToClimbForPlayer(player, server);
		}
	}
}