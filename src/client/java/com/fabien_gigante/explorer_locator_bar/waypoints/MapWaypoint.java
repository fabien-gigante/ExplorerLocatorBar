package com.fabien_gigante.explorer_locator_bar.waypoints;

import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.MapDecorations.Entry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.PartialTickSupplier;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import org.jetbrains.annotations.Nullable;
import com.fabien_gigante.explorer_locator_bar.IMapDecorationsEntryExt;
import com.fabien_gigante.explorer_locator_bar.MapWaypointStyleAssets;

public class MapWaypoint extends NamedWaypoint {
    private boolean hasY = false;

    protected MapWaypoint(String source, ResourceKey<WaypointStyleAsset> style, @Nullable Integer color, Vec3i pos, Optional<Component> name) {
        super(source, configFromStyle(style, color), pos, name);
    }
    public MapWaypoint(String source, Entry deco, Optional<Component> name) {
        this(source, MapWaypointStyleAssets.getStyle(deco.type()), MapWaypointStyleAssets.getColor(deco.type()), getPosOf(deco), name);
        hasY =  (Object)deco instanceof IMapDecorationsEntryExt ext && ext.getY().isPresent();
    }
    public MapWaypoint(String source, MapDecoration deco, Vec3i pos) {
        this(source, MapWaypointStyleAssets.getStyle(deco.type()), MapWaypointStyleAssets.getColor(deco.type()), pos, deco.name());
    }
    
    private static Vec3i getPosOf(Entry deco) {
        double x = Math.floor(deco.x());
        double y = (Object)deco instanceof IMapDecorationsEntryExt ext ? Math.floor(ext.getY().orElse(0d)) : 0d;
        double z = Math.floor(deco.z());
        return new Vec3i((int)x, (int)y, (int)z);
    }

    @Override
    public double distanceSquared(Entity receiver) {
        if (hasY) return super.distanceSquared(receiver);
        Vec3 pos = Vec3.atCenterOf(this.vector);
        double x = receiver.getX() - pos.x, z = receiver.getZ() - pos.z;
        return x*x + z*z;
    }

    @Override
    public TrackedWaypoint.PitchDirection pitchDirectionToCamera(Level world, TrackedWaypoint.Projector cameraProvider, PartialTickSupplier tickProgress) {
        return hasY ? super.pitchDirectionToCamera(world, cameraProvider, tickProgress) : PitchDirection.NONE;
    }

    public static class Config extends Waypoint.Icon {
        public Optional<Integer> textColor = Optional.empty();
    }

    private static Config configFromStyle(ResourceKey<WaypointStyleAsset> style, @Nullable Integer color) {
        Config config = new Config();
        config.style = style;
        config.color = Optional.of(CommonColors.WHITE);
        config.textColor = color == -1 ? Optional.empty() : Optional.of(ARGB.color(255, color));
        return config;
    }
}