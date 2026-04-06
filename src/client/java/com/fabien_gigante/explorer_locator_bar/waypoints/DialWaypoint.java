package com.fabien_gigante.explorer_locator_bar.waypoints;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.PartialTickSupplier;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAsset;
import com.fabien_gigante.explorer_locator_bar.config.ConfigManager;

public class DialWaypoint extends TrackedWaypoint.AzimuthWaypoint {
    protected DialWaypoint(String source, Icon config, float azimuth) {
        super(Either.right(source), config, bufFromFloat(azimuth));
    }
    
    public DialWaypoint(String source, ResourceKey<WaypointStyleAsset> style, float azimuth) {
        this(source, configFromStyle(style), azimuth);
    }

    @Override
    public TrackedWaypoint.PitchDirection pitchDirectionToCamera(Level world, TrackedWaypoint.Projector cameraProvider, PartialTickSupplier tickProgress) {
        return PitchDirection.NONE;
    }
    
    @Override
    public double distanceSquared(Entity receiver) {
        // greater than Double.POSITIVE_INFINITY, so that it always renders in the back
        return Double.NaN; 
    }

    private static Icon configFromStyle(ResourceKey<WaypointStyleAsset> style) {
        Waypoint.Icon config = new Waypoint.Icon();
        config.style = style;
        config.color = Optional.of(ConfigManager.getConfig().colors().dialColor().getColorWithAlpha(160));
        return config;
    }
    
    private static FriendlyByteBuf bufFromFloat(float f) {
        FriendlyByteBuf buf = FriendlyByteBufs.create();
        buf.writeFloat(f);
        return buf;
    }
}
