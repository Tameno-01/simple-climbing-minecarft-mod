package com.tameno.simpleclimbing.mixin;

import com.tameno.simpleclimbing.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
@Inject(method = "isClimbing", at = @At("HEAD"), cancellable = true)
    public void isClimbingInject(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity thisEntity = (LivingEntity)(Object)this;
        if (
                (thisEntity instanceof PlayerEntity)
                && (!thisEntity.isOnGround())
                && Utils.isTouchingWall(thisEntity)
        ) {
            cir.setReturnValue(true);
        }
    }
}
