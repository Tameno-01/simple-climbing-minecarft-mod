package com.tameno.simpleclimbing;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

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

	public static final Identifier SET_CLIMBING_ALLOWED = new Identifier(MOD_ID, "set_climbing_allowed");

	@Override
	public void onInitialize() {

		ServerPlayConnectionEvents.JOIN.register((
				ServerPlayNetworkHandler handler,
				PacketSender sender,
				MinecraftServer server
		) -> {
			updateAbilityToClimbForPlayer(handler.getPlayer(), server);
		});

		// /enableclimbing <true, false, default> <player>
		CommandRegistrationCallback.EVENT.register((
				CommandDispatcher<ServerCommandSource> commandDispatcher,
				CommandRegistryAccess commandRegistryAccess,
				CommandManager.RegistrationEnvironment registrationEnvironment
		) -> commandDispatcher.register(
			CommandManager.literal("enableclimbing")
			.requires((ServerCommandSource source) -> source.hasPermissionLevel(1))
			.then(
				CommandManager.argument("enabled", StringArgumentType.string())
				.suggests(new TrueFalseDefaultSuggester())
				.then(
					CommandManager.argument("player", StringArgumentType.string())
					.suggests(new PlayerSuggester())
					.executes(SimpleClimbing::runCommandAllowClimbing)
				)
			)
		));
	}

	public static void updateAbilityToClimbForPlayer(ServerPlayerEntity player, MinecraftServer server) {
		StateSaverAndLoader state = StateSaverAndLoader.getServerState(server);
		PlayerData.ClimbAbility climbAbility = state.getPlayerClimbingAbility(player.getUuid());
		boolean canClimb = true;
		if (climbAbility == PlayerData.ClimbAbility.DEFAULT) {
			canClimb = server.getGameRules().getBoolean(ENABLE_CLIMBING_BY_DEFAULT);
		} else if (climbAbility == PlayerData.ClimbAbility.FALSE) {
			canClimb = false;
		}
		PacketByteBuf buff = PacketByteBufs.create();
		buff.writeBoolean(canClimb);
		ServerPlayNetworking.send(player, SET_CLIMBING_ALLOWED, buff);
	}

	public static void updateAbilityToClimbForAllPlayers(MinecraftServer server) {
		for (ServerPlayerEntity player : PlayerLookup.all(server)) {
			updateAbilityToClimbForPlayer(player, server);
		}
	}

	// /enableclimbing <true, false, default> <player>
	public static int runCommandAllowClimbing(
			CommandContext<ServerCommandSource> context
	) throws CommandSyntaxException {
		String climbAbilityName = StringArgumentType.getString(context, "enabled");
		PlayerData.ClimbAbility climbAbility = switch (climbAbilityName) {
			case "default" -> PlayerData.ClimbAbility.DEFAULT;
			case "true" -> PlayerData.ClimbAbility.TRUE;
			case "false" -> PlayerData.ClimbAbility.FALSE;
			default -> throw new SimpleCommandExceptionType(Text.literal(
					"\"" + climbAbilityName + "\" is invalid. Must be \"default\", \"true\" or \"false\"."
			)).create();
		};
		MinecraftServer server = context.getSource().getServer();
		String playerName = StringArgumentType.getString(context, "player");
		ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
		if (player == null) {
			throw new SimpleCommandExceptionType(Text.literal(
					"Couldn't find player \"" + playerName + "\".")
			).create();
		}
		StateSaverAndLoader state = StateSaverAndLoader.getServerState(server);
		state.setPlayerClimbingAbility(player.getUuid(), climbAbility);
		updateAbilityToClimbForPlayer(player, server);
		context.getSource().sendFeedback(() -> Text.literal(switch (climbAbility) {
					case DEFAULT ->  playerName + " now follows the gamerule \"enableClimbingByDefault\".";
					case TRUE ->  playerName + " can now climb anything.";
					case FALSE ->  playerName + " can now only climb ladders and similar blocks.";
				}),
				true
		);
		return 1;
	}
}