package com.fabien_gigante.explorer_locator_bar.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.fabien_gigante.explorer_locator_bar.ComponentsHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;

@Mixin(CompassItem.class)
public abstract class CompassItemMixin {
    @Redirect(
        method = "useOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"
        )
    )
    private <T> T setLodestoneComponents(ItemStack stack, DataComponentType<T> type, T value) {
        ComponentsHelper.cycleLodestoneTrackerColor(stack, (LodestoneTracker) value);
        return stack.set(type, value);
    }
}