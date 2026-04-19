package com.fabien_gigante.explorer_locator_bar.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.fabien_gigante.explorer_locator_bar.IDecorationExt;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;

@Mixin(ExplorationMapFunction.class)
public class ExplorationMapFunctionMixin {
    @Inject(method = "run", at = @At("RETURN"))
    private static void onRun(ItemStack itemStack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack map = cir.getReturnValue();
        MapDecorations decorations = map.get(DataComponents.MAP_DECORATIONS);
        if (decorations == null) return;
        decorations.decorations().values().stream()
            .filter(IDecorationExt.class::isInstance).map(IDecorationExt.class::cast)
            .forEach(ext -> ext.setDimension(Optional.of(context.getLevel().dimension())));
    }
}
