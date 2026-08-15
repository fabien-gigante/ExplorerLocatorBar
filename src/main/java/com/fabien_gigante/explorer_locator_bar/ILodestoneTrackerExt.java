package com.fabien_gigante.explorer_locator_bar;

import net.minecraft.world.item.component.LodestoneTracker;

public interface ILodestoneTrackerExt {
    public int getColor();
    public void cycleColor(LodestoneTracker previous);
}
