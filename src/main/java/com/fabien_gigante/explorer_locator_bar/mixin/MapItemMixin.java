package com.fabien_gigante.explorer_locator_bar.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.fabien_gigante.explorer_locator_bar.MapComponentsHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.context.UseOnContext;

@Mixin(MapItem.class)
public abstract class MapItemMixin {
    @Inject(
        method = "useOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;toggleBanner(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)Z",
            shift = At.Shift.AFTER
        )
    )
    private void updateBannerComponent(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        MapComponentsHelper.updateBannerComponent(context.getLevel(), context.getItemInHand(), context.getClickedPos());
    }

    @Inject(
        method = "inventoryTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/MapItem;update(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;)V",
            shift = At.Shift.AFTER
        )
    )
    private void updateBannerComponents(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot, CallbackInfo ci) {
        MapComponentsHelper.updateBannerComponents(world, stack);
    }
}
