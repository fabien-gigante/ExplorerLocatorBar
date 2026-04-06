package com.fabien_gigante.explorer_locator_bar.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.fabien_gigante.explorer_locator_bar.MapComponentsHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

@Mixin(MapItemSavedData.class)
public abstract class MapItemSavedDataMixin {
    @Shadow @Final
    public ResourceKey<Level> dimension;
    
    // MC-142687 : on Nether maps, banners appear randomly rotated, let's fix that
    @Inject(method = "calculateDecorationLocationAndType", at = @At("RETURN"), cancellable = true)
    private void fixBannerRotation(Holder<MapDecorationType> type, @Nullable LevelAccessor world, double rotation, float dx, float dz, CallbackInfoReturnable<MapItemSavedData.MapDecorationLocation> cir) {
        MapItemSavedData.MapDecorationLocation marker = cir.getReturnValue();
        if (marker != null && this.dimension == Level.NETHER && MapComponentsHelper.isBanner(type))
            cir.setReturnValue(new MapItemSavedData.MapDecorationLocation(type, marker.x(), marker.y(), (byte)8));
    }

    // MC-142686 : banners can be added / removed on maps from other dimensions, let's fix that
    @Inject(method = "toggleBanner", at = @At("HEAD"), cancellable = true)
    private void checkBannerDimension(LevelAccessor worldAccess, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (worldAccess instanceof Level world && world.dimension() != this.dimension) {
            cir.setReturnValue(false); cir.cancel();
        }
    }

    /*
    NOTE: Structures don't seem to have a relevant Y, so the following is counter-productive after all...

    @Inject(method = "addDecorationsNbt", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void extendDecoration(ItemStack stack, BlockPos pos, String id, RegistryEntry<MapDecorationType> decorationType, CallbackInfo ci, Decoration decoration) {
        if ((Object) decoration instanceof IDecorationExt ext) ext.setY(Optional.of(pos.toCenterPos().getY()));
    }
    */
}