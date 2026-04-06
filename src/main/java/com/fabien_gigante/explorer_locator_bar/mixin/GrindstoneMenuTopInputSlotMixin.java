package com.fabien_gigante.explorer_locator_bar.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fabien_gigante.explorer_locator_bar.ISlotListener;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Mixin(targets = "net/minecraft/world/inventory/GrindstoneMenu$2")
public class GrindstoneMenuTopInputSlotMixin extends Slot {
	@Unique @Final private GrindstoneMenu grindstoneMenu;

	public GrindstoneMenuTopInputSlotMixin(Container inventory, int index, int x, int y) { super(inventory, index, x, y); }

	// Cache grindstone parent
	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(GrindstoneMenu grindstoneMenu, Container inventory, int i, int j, int k, CallbackInfo ci) {
		this.grindstoneMenu = grindstoneMenu;
	}

	// Grindstone parent can allow additional items as input 
	@ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
	private boolean modifyCanInsert(boolean original, ItemStack stack) {
		return original || ((ISlotListener) grindstoneMenu).isValidInput(this, stack);
	}
}
