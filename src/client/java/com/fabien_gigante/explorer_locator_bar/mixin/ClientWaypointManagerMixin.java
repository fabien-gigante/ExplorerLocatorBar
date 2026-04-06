package com.fabien_gigante.explorer_locator_bar.mixin;

import com.fabien_gigante.explorer_locator_bar.IWaypointAccessor;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.waypoints.ClientWaypointManager;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.datafixers.util.Either;

@Mixin(ClientWaypointManager.class)
public abstract class ClientWaypointManagerMixin implements IWaypointAccessor {
	@Shadow @Final private Map<Either<UUID, String>, TrackedWaypoint> waypoints;

    public Collection<TrackedWaypoint> getWaypointsUnsorted() {
        return this.waypoints.values();
    }
}
