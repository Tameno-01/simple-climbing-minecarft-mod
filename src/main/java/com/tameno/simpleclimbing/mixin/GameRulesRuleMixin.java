package com.tameno.simpleclimbing.mixin;

import com.tameno.simpleclimbing.SimpleClimbing;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRules.Rule.class)
public class GameRulesRuleMixin {
    @Inject(method = "changed", at = @At("TAIL"))
    private void changedInject(@Nullable MinecraftServer server, CallbackInfo ci) {
        if (server != null) {
            SimpleClimbing.updateAbilityToClimbForAllPlayers(server);
        }
    }
}
