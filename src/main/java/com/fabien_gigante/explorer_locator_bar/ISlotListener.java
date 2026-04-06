package com.fabien_gigante.explorer_locator_bar;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public interface ISlotListener {
	public boolean isValidInput(Slot slot, ItemStack stack);
}