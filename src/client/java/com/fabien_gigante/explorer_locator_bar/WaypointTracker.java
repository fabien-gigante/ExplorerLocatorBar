package com.fabien_gigante.explorer_locator_bar;

import com.mojang.datafixers.util.Either;
import com.fabien_gigante.explorer_locator_bar.config.ConfigManager;
import com.fabien_gigante.explorer_locator_bar.waypoints.DialWaypoint;
import com.fabien_gigante.explorer_locator_bar.waypoints.MapWaypoint;
import com.fabien_gigante.explorer_locator_bar.waypoints.NamedWaypoint;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.waypoints.ClientWaypointManager;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.waypoints.TrackedWaypoint;

public class WaypointTracker extends AbstractTracker {
    private static final List<TrackedWaypoint> COMPASS_DIAL_WAYPOINTS = new ArrayList<>();
    private final Map<Either<UUID, String>, TrackedWaypoint> WAYPOINTS = new HashMap<>();

    @Override
    public void init() {
        super.init();
        this.buildCompassDial();
    }

    @Override
    public void reset() {
        super.reset();
        WAYPOINTS.clear();
        this.buildCompassDial();
    }

    public Collection<TrackedWaypoint> getWaypoints() {
        return WAYPOINTS.values();
    }

    private void buildCompassDial() {
        COMPASS_DIAL_WAYPOINTS.clear();
        int dialResolution = ConfigManager.getConfig().dialResolution();
        for(int i = 0; i < dialResolution; i++) {
            int azimuth = i * 360 / dialResolution;
            var style = azimuth % 90 == 0 ? ExplorerLocatorBar.COMPASS_CARDINAL_STYLE.get(azimuth / 90) :
                        azimuth % 45 == 0 ? ExplorerLocatorBar.COMPASS_DIVISION_STYLE : ExplorerLocatorBar.COMPASS_DIVISION_SMALL_STYLE;
            COMPASS_DIAL_WAYPOINTS.add(new DialWaypoint("dial_" + azimuth, style, (float)(i * Math.TAU / dialResolution)));
        }
    }    

    @Override
    public void update(Minecraft client) {
        LocalPlayer player = client.player;

        Map<Either<UUID, String>, TrackedWaypoint> oldWaypoints = new HashMap<>(WAYPOINTS);
        WAYPOINTS.clear();
        getWaypointsFromPlayer(player).forEach(waypoint -> WAYPOINTS.put(waypoint.id(), waypoint));

        ClientWaypointManager waypointHandler = player.connection.getWaypointManager();
        for (TrackedWaypoint newWaypoint : WAYPOINTS.values()) {
            if (oldWaypoints.containsKey(newWaypoint.id())) {
                waypointHandler.updateWaypoint(newWaypoint);
            } else {
                waypointHandler.trackWaypoint(newWaypoint);
            }
        }
        for (TrackedWaypoint oldWaypoint : oldWaypoints.values()) {
            if (!WAYPOINTS.containsKey(oldWaypoint.id())) {
                waypointHandler.untrackWaypoint(oldWaypoint);
            }
        }
    }

    private static List<ItemStack> getPlayerStacks(Player player) {
        return getPlayerStacks(player, ConfigManager.getConfig().holdingLocation());
    }

    private static List<TrackedWaypoint> getWaypointsFromPlayer(Player player) {
        List<TrackedWaypoint> waypoints = new ArrayList<>();
        List<ItemStack> stacks = getPlayerStacks(player);

        if (ConfigManager.getConfig().dialResolution() > 0) {
            boolean withMaps = ConfigManager.getConfig().showMaps();
            if (stacks.stream().anyMatch(stack -> stack.is(Items.COMPASS) || stack.is(Items.RECOVERY_COMPASS) || (withMaps && stack.is(Items.FILLED_MAP))))
                waypoints.addAll(COMPASS_DIAL_WAYPOINTS);
        }

        ResourceKey<Level> dimension = player.level().dimension();
        for (ItemStack stack : stacks)
            waypoints.addAll(getWaypointsFromStack(player, dimension, stack));
        return waypoints;
    }

    private static List<TrackedWaypoint> getWaypointsFromStack(Player player, ResourceKey<Level> dimension, ItemStack stack) {
        List<TrackedWaypoint> waypoints = new ArrayList<>();
        Level world = player.level();

        if (ConfigManager.getConfig().showRecovery()) {
            Optional<GlobalPos> lastDeathPos = player.getLastDeathLocation();
            if (lastDeathPos.isPresent() && stack.is(Items.RECOVERY_COMPASS)) {
                GlobalPos pos = lastDeathPos.get();
                if (pos.dimension() == dimension && pos.pos() != null) {
                    Integer color = ColorHandler.getColor(stack).orElse(ConfigManager.getConfig().colors().recoveryColor().getColorWithAlpha());
                    TrackedWaypoint waypoint = new NamedWaypoint("death_" + pos, ExplorerLocatorBar.DEATH_STYLE, color, pos.pos(), getText(stack));
                    waypoints.add(waypoint);
                }
            }
        }

        LodestoneTracker trackerComponent = stack.get(DataComponents.LODESTONE_TRACKER);
        if (trackerComponent != null && trackerComponent.target().isPresent()) {
            GlobalPos pos = trackerComponent.target().get();
            if (pos.dimension() == dimension && pos.pos() != null) {
                Integer defaultColor = ConfigManager.getConfig().colors().lodestoneColor().getColorWithAlpha();
                Integer color = ColorHandler.getColor(stack, defaultColor == null).orElse(defaultColor);
                TrackedWaypoint waypoint = new NamedWaypoint("lodestone_" + pos + "_" + color, ExplorerLocatorBar.LODESTONE_STYLE, color, pos.pos(), getText(stack));
                waypoints.add(waypoint);
            }
        }

        if (ConfigManager.getConfig().showSpawn() && stack.is(Items.COMPASS) && trackerComponent == null) {
            GlobalPos pos = world.getRespawnData().globalPos();
            if (pos.dimension() == dimension && pos.pos() != null) {
                Integer color = ColorHandler.getColor(stack).orElse(ConfigManager.getConfig().colors().spawnColor().getColorWithAlpha());
                TrackedWaypoint waypoint = new NamedWaypoint("spawn_" + pos, ExplorerLocatorBar.SPAWN_STYLE, color, pos.pos(), getText(stack));
                waypoints.add(waypoint);
            }
        }

        if (ConfigManager.getConfig().showMaps() && stack.is(Items.FILLED_MAP)) {
            MapId mapIdComponent = stack.get(DataComponents.MAP_ID);
            MapDecorations mapDecorationsComponent = stack.get(DataComponents.MAP_DECORATIONS);
            if (mapIdComponent != null && mapDecorationsComponent != null) {
                mapDecorationsComponent.decorations().forEach((key, deco) -> {
                    Optional<ResourceKey<Level>> mapDimension = (Object)deco instanceof IDecorationExt ext ? ext.getDimension() : Optional.empty();
                    if (mapDimension.orElse(Level.OVERWORLD) == dimension) {
                        Optional<Component> name = (Object)deco instanceof IDecorationExt ext ? ext.getName() : Optional.empty();
                        if (name.isEmpty()) name = getText(stack);
                        TrackedWaypoint waypoint = new MapWaypoint("map_" + mapIdComponent.id() + "_" + key, deco, name);
                        waypoints.add(waypoint);
                    }
                });
            }
        }

        return waypoints;
    }

    private static Optional<Component> getText(ItemStack stack) {
        Component text = stack.get(DataComponents.CUSTOM_NAME);
        if (text == null) {
            text = stack.get(DataComponents.ITEM_NAME);
        }
        return ColorHandler.removeColorCode(text);
    }
}
