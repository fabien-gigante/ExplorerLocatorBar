package com.fabien_gigante.explorer_locator_bar;

import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.WaypointStyle;
import net.minecraft.client.resources.WaypointStyleManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import java.util.HashMap;
import java.util.List;

public class MapWaypointStyleAssets {
    private static Map<Holder<MapDecorationType>, ResourceKey<WaypointStyleAsset>> STYLES
        = new HashMap<Holder<MapDecorationType>, ResourceKey<WaypointStyleAsset>>();

    private static final Map<Holder<MapDecorationType>, Integer> COLORS = Map.ofEntries(
        Map.entry(MapDecorationTypes.RED_X,             MapColor.FIRE.col),
        Map.entry(MapDecorationTypes.RED_MARKER,        MapColor.FIRE.col),
        Map.entry(MapDecorationTypes.BLUE_MARKER,       MapColor.COLOR_BLUE.col),
        Map.entry(MapDecorationTypes.TARGET_POINT,      MapColor.FIRE.col),
        Map.entry(MapDecorationTypes.TARGET_X,          MapColor.SNOW.col),
        Map.entry(MapDecorationTypes.FRAME,             MapColor.COLOR_GREEN.col),
        Map.entry(MapDecorationTypes.WHITE_BANNER,      MapColor.SNOW.col), 
        Map.entry(MapDecorationTypes.ORANGE_BANNER,     MapColor.COLOR_ORANGE.col), 
        Map.entry(MapDecorationTypes.MAGENTA_BANNER,    MapColor.COLOR_MAGENTA.col),
        Map.entry(MapDecorationTypes.LIGHT_BLUE_BANNER, MapColor.COLOR_LIGHT_BLUE.col),
        Map.entry(MapDecorationTypes.YELLOW_BANNER,     MapColor.COLOR_YELLOW.col),
        Map.entry(MapDecorationTypes.LIME_BANNER,       MapColor.COLOR_LIGHT_GREEN.col),
        Map.entry(MapDecorationTypes.PINK_BANNER,       MapColor.COLOR_PINK.col),
        Map.entry(MapDecorationTypes.GRAY_BANNER,       MapColor.COLOR_GRAY.col),
        Map.entry(MapDecorationTypes.LIGHT_GRAY_BANNER, MapColor.COLOR_LIGHT_GRAY.col),
        Map.entry(MapDecorationTypes.CYAN_BANNER,       MapColor.COLOR_CYAN.col),
        Map.entry(MapDecorationTypes.PURPLE_BANNER,     MapColor.COLOR_PURPLE.col),
        Map.entry(MapDecorationTypes.BLUE_BANNER,       MapColor.COLOR_BLUE.col),
        Map.entry(MapDecorationTypes.BROWN_BANNER,      MapColor.COLOR_BROWN.col),
        Map.entry(MapDecorationTypes.GREEN_BANNER,      MapColor.COLOR_GREEN.col),
        Map.entry(MapDecorationTypes.RED_BANNER,        MapColor.COLOR_RED.col),
        Map.entry(MapDecorationTypes.BLACK_BANNER,      MapColor.COLOR_BLACK.col));

    private static final int NEAR_DISTANCE = 8 * WaypointStyle.DEFAULT_NEAR_DISTANCE;
    private static final int FAR_DISTANCE = 8 * WaypointStyle.DEFAULT_FAR_DISTANCE;

    public static void reload() {
        Minecraft client = Minecraft.getInstance();
        WaypointStyleManager assetManager = client.gui.hud.getWaypointStyles();
        assetManager.waypointStyles = new HashMap<>(assetManager.waypointStyles); // Make it mutable
        STYLES.clear();
        BuiltInRegistries.MAP_DECORATION_TYPE.listElements().forEach(type -> {
            Identifier id = ExplorerLocatorBar.id(type.value().assetId().getPath());
            ResourceKey<WaypointStyleAsset> style = ExplorerLocatorBar.style("map_" + id.getPath());
            STYLES.put(type, style);
            WaypointStyle asset = new WaypointStyle(NEAR_DISTANCE, FAR_DISTANCE,
                List.of(id.withSuffix("_0"), id.withSuffix("_1")),
                List.of(id.withPrefix("hud/locator_bar_dot/map/"), id.withPrefix("hud/locator_bar_dot/map/small/")));
            assetManager.waypointStyles.put(style, asset);
        });
    }

    public static ResourceKey<WaypointStyleAsset> getStyle(Holder<MapDecorationType> type) {
        return STYLES.get(type);
    }
    public static int getColor(Holder<MapDecorationType> type) {
        int color = type.value().hasMapColor() ? type.value().mapColor() : COLORS.getOrDefault(type, MapDecorationType.NO_MAP_COLOR);
        return ARGB.opaque(color);
    }
    
}