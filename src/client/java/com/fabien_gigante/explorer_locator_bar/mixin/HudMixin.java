package com.fabien_gigante.explorer_locator_bar.mixin;

import com.fabien_gigante.explorer_locator_bar.WaypointRenderer;
import com.fabien_gigante.explorer_locator_bar.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.client.waypoints.ClientWaypointManager;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Shadow @Final private Minecraft minecraft;

    @Redirect(
        method = "nextContextualInfoState",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/waypoints/ClientWaypointManager;hasWaypoints()Z")
    )
    private boolean noLocatorBarWhenPlayerIsSpectator(ClientWaypointManager waypointManager) {
        if ((minecraft.player == null || minecraft.player.isSpectator()) && !ConfigManager.getConfig().showInSpectator()) return false;
        else return waypointManager.hasWaypoints();
    }

    @Inject(method = "nextContextualInfoState", at = @At("HEAD"), cancellable = true)
    private void forceLocatorBarWhenPlayerListOpen(CallbackInfoReturnable<Hud.ContextualInfo> info) {
        boolean canShow = ConfigManager.getConfig().tabForcesLocatorBar()
                && (ConfigManager.getConfig().showInSpectator() || (minecraft.player != null && !minecraft.player.isSpectator()));
        if (canShow && minecraft.options.keyPlayerList.isDown()) info.setReturnValue(Hud.ContextualInfo.LOCATOR);
    }

    @Redirect(
        method = "extractHotbarAndDecorations",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V")
    )
    private void conditionallyExtractExperienceLevel(GuiGraphicsExtractor context, Font textRenderer, int level) {
        if (WaypointRenderer.shouldShowExperienceLevel())
            ContextualBar.extractExperienceLevel(context, textRenderer, level);
    }    
}
