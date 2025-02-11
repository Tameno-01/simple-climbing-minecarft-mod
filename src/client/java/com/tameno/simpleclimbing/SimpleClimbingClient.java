package com.tameno.simpleclimbing;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class SimpleClimbingClient implements ClientModInitializer {

	public static boolean canClimb = false;

	@Override
	public void onInitializeClient() {

		ClientPlayNetworking.registerGlobalReceiver(SimpleClimbing.SET_CLIMBING_ALLOWED, (
				MinecraftClient client,
				ClientPlayNetworkHandler handler,
				PacketByteBuf buffer,
				PacketSender sender
		) -> {
			canClimb = buffer.readBoolean();
		});

	}
}