package com.fabien_gigante.explorer_locator_bar.mixin;

import com.fabien_gigante.explorer_locator_bar.WaypointRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.client.gui.contextualbar.LocatorBar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocatorBar.class)
public abstract class LocatorBarMixin implements ContextualBar {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "extractRenderState",at = @At("RETURN"))
    private void extractClientWaypoints(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        WaypointRenderer.render(this.minecraft, context, tickCounter, this.top(this.minecraft.getWindow()));
    }
}
