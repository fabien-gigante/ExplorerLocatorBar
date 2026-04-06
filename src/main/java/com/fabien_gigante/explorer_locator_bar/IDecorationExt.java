package com.fabien_gigante.explorer_locator_bar;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IDecorationExt {
    Optional<Double> getY();
    void setY(Optional<Double> y);
    Optional<Component> getName();
    void setName(Optional<Component> name);
    Optional<ResourceKey<Level>> getDimension();
    void setDimension(Optional<ResourceKey<Level>> dimension);
}
