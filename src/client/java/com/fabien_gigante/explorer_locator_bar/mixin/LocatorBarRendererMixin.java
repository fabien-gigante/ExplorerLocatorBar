package com.fabien_gigante.explorer_locator_bar.mixin;

import com.fabien_gigante.explorer_locator_bar.WaypointRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocatorBarRenderer.class)
public abstract class LocatorBarRendererMixin implements ContextualBarRenderer {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "extractRenderState",at = @At("RETURN"))
    private void extractClientWaypoints(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        WaypointRenderer.render(this.minecraft, context, tickCounter, this.top(this.minecraft.getWindow()));
    }
}
