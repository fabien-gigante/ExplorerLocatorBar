package com.fabien_gigante.explorer_locator_bar;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class ComponentsHelper {
    private static Set<Holder<MapDecorationType>> BANNER_TYPES = Set.of(
        MapDecorationTypes.WHITE_BANNER, MapDecorationTypes.ORANGE_BANNER, MapDecorationTypes.MAGENTA_BANNER, MapDecorationTypes.LIGHT_BLUE_BANNER,
	    MapDecorationTypes.YELLOW_BANNER, MapDecorationTypes.LIME_BANNER, MapDecorationTypes.PINK_BANNER, MapDecorationTypes.GRAY_BANNER,
        MapDecorationTypes.LIGHT_GRAY_BANNER, MapDecorationTypes.CYAN_BANNER, MapDecorationTypes.PURPLE_BANNER, MapDecorationTypes.BLUE_BANNER,
        MapDecorationTypes.BROWN_BANNER, MapDecorationTypes.GREEN_BANNER, MapDecorationTypes.RED_BANNER, MapDecorationTypes.BLACK_BANNER);

    public static boolean isBanner(Holder<MapDecorationType> type) { return BANNER_TYPES.contains(type); }

    private static void addBannerComponent(ItemStack stack, Level world, BlockPos pos, String id, Holder<MapDecorationType> decorationType, Optional<Component> name) {
        MapDecorations.Entry decoration = new MapDecorations.Entry(decorationType, (double)pos.getX(), (double)pos.getZ(), 180.0F);
        if ((Object)decoration instanceof IMapDecorationsEntryExt ext) {
            ext.setName(name);  ext.setY(Optional.of((double)pos.getY()));
            ext.setDimension(Optional.of(world.dimension()));
        }
        stack.update(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY, decorations -> decorations.withDecoration(id, decoration));
    }

    private static void removeBannerComponents(ItemStack stack, Predicate<String> condition) {
        MapDecorations component = stack.get(DataComponents.MAP_DECORATIONS);
        if (component == null || component.decorations().isEmpty()) return;
        var map = new HashMap<>(component.decorations());
        if (map.entrySet().removeIf(entry -> isBanner(entry.getValue().type()) && condition.test(entry.getKey())))
            stack.set(DataComponents.MAP_DECORATIONS, new MapDecorations(map));
    }

    private static boolean needsUpdate(MapItemSavedData mapState, Level world) {
        return mapState != null && mapState.isDirty() & mapState.dimension == world.dimension();
    }

    public static void updateBannerComponent(Level world, ItemStack stack, BlockPos pos) {
        MapItemSavedData mapState = MapItem.getSavedData(stack, world);
        if (!needsUpdate(mapState, world)) return;
        var found = mapState.getBanners().stream()
            .filter(m -> m.pos().getX() == pos.getX() && m.pos().getZ() == pos.getZ() && isBanner(m.getDecoration()))
            .findAny();
        if (found.isPresent())
            addBannerComponent(stack, world, pos, found.get().getId(), found.get().getDecoration(), found.get().name());
        else {
            MapBanner marker = MapBanner.fromWorld(world, pos);
            removeBannerComponents(stack, key -> key.equals(marker.getId()));
        }
    }

    public static void updateBannerComponents(Level world, ItemStack stack) {
        MapItemSavedData mapState = MapItem.getSavedData(stack, world);
        if (!needsUpdate(mapState, world)) return;
        var keys = mapState.getBanners().stream().map(MapBanner::getId).collect(Collectors.toSet());
        removeBannerComponents(stack, key -> !keys.contains(key));
    }
     
    public static void cycleLodestoneTrackerColor(ItemStack stack, LodestoneTracker newTracker) {
        ILodestoneTrackerExt trackerExt = (ILodestoneTrackerExt)(Object)newTracker;
        if (trackerExt != null) trackerExt.cycleColor(stack.get(DataComponents.LODESTONE_TRACKER));
    }

    public static Optional<Integer> getLodestoneTrackerColor(ItemStack stack) {
        ILodestoneTrackerExt trackerExt = (ILodestoneTrackerExt)(Object)stack.get(DataComponents.LODESTONE_TRACKER);
        return trackerExt != null ? Optional.of(trackerExt.getColor()) : Optional.empty();
    }
}