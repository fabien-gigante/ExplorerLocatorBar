package com.fabien_gigante.explorer_locator_bar.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {
    private LocalPlayerMixin(Level world, GameProfile profile) { super(world, profile); }

    @Inject(method = "setExperienceValues", at = @At("HEAD"), cancellable = true)
    public void setExperienceValues(float progress, int total, int level, CallbackInfo ci) {
        if (progress==experienceProgress && total==totalExperience && level==experienceLevel)
            ci.cancel();
    }
}