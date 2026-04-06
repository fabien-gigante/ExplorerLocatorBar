package com.fabien_gigante.explorer_locator_bar.waypoints;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointStyleAsset;

public class NamedWaypoint extends TrackedWaypoint.Vec3iWaypoint {
    protected Optional<Component> name = Optional.empty();

    protected NamedWaypoint(String source, Icon config, Vec3i pos, Optional<Component> name) {
        super(Either.right(source), config, bufFromPos(pos));
        this.name = name;
    }
    public NamedWaypoint(String source, ResourceKey<WaypointStyleAsset> style, @Nullable Integer color, Vec3i pos, Optional<Component> name) {
        this(source, configFromStyle(style, color), pos, name);
    }

    public Optional<Component> getName() { return name; }

    private static Icon configFromStyle(ResourceKey<WaypointStyleAsset> style, @Nullable Integer color) {
        Waypoint.Icon config = new Waypoint.Icon();
        config.style = style;
        config.color = Optional.ofNullable(color);
        return config;
    }
    
    private static FriendlyByteBuf bufFromPos(Vec3i pos) {
        FriendlyByteBuf buf = FriendlyByteBufs.create();
        buf.writeVarInt(pos.getX());
        buf.writeVarInt(pos.getY());
        buf.writeVarInt(pos.getZ());
        return buf;
    }
}
