package com.fabien_gigante.explorer_locator_bar.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.fabien_gigante.explorer_locator_bar.ISlotListener;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin extends AbstractContainerMenu implements ISlotListener {
	@Shadow @Final Container repairSlots;
	@Shadow @Final ContainerLevelAccess access;    

	protected GrindstoneMenuMixin(MenuType<?> type, int syncId) { super(type, syncId); }

	@ModifyReturnValue(method = "computeResult", at = @At("RETURN"))
	private ItemStack modifyOutputStack(ItemStack original, ItemStack firstInput, ItemStack secondInput) {
		if (original != ItemStack.EMPTY || !isValidLodestoneTrackerRecipe(firstInput, secondInput)) return original;
		ItemStack result = firstInput.copy();
		result.remove(DataComponents.LODESTONE_TRACKER);
		result.remove(DataComponents.MAP_COLOR);
		return result;
	}

	public boolean isValidInput(Slot slot, ItemStack stack) {
		return slot == this.getSlot(0) && hasLodestoneTracker(stack);
	}

	@Unique
	private boolean hasLodestoneTracker(ItemStack stack) {
        return stack.get(DataComponents.LODESTONE_TRACKER) != null;
	}

	@Unique
	private boolean isValidLodestoneTrackerRecipe(ItemStack firstInput, ItemStack secondInput) {
		return (secondInput == null || secondInput.isEmpty()) && hasLodestoneTracker(firstInput);
	}
}
