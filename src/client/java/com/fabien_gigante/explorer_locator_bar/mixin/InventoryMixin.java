package com.fabien_gigante.explorer_locator_bar.mixin;

import com.fabien_gigante.explorer_locator_bar.ExplorerLocatorBar;
import com.fabien_gigante.explorer_locator_bar.config.Config;
import com.fabien_gigante.explorer_locator_bar.config.ConfigManager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Inject(method = "setChanged", at = @At("RETURN"))
    private void updateWaypointsOnChanged(CallbackInfo ci) {
        ExplorerLocatorBar.onInventoryChanged();
    }

    @Inject(method = "removeFromSelected", at = @At("RETURN"))
    private void updateWaypointsOnItemDrop(boolean entireStack, CallbackInfoReturnable<ItemStack> cir) {
        ExplorerLocatorBar.onInventoryChanged();
    }

    @Inject(method = "setSelectedSlot", at = @At("RETURN"))
    private void updateWaypointsOnSelection(int slot, CallbackInfo ci) {
        if (ConfigManager.getConfig().holdingLocation() == Config.HoldingLocation.HANDS)
            ExplorerLocatorBar.onInventoryChanged();
    }
}
