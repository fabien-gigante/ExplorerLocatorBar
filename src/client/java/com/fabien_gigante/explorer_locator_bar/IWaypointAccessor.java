package com.fabien_gigante.explorer_locator_bar;

import java.util.Collection;
import net.minecraft.world.waypoints.TrackedWaypoint;

public interface IWaypointAccessor {
    public Collection<TrackedWaypoint> getWaypointsUnsorted();
}