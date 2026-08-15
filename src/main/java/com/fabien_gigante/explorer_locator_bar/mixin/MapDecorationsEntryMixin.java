package com.fabien_gigante.explorer_locator_bar.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.fabien_gigante.explorer_locator_bar.IDecorationExt;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

@Mixin(MapDecorations.Entry.class)
public abstract class MapDecorationsEntryMixin implements IDecorationExt {
    @Shadow @Final @Mutable
    public static Codec<MapDecorations.Entry> CODEC;

    private Optional<Component> name = Optional.empty();
    private Optional<ResourceKey<Level>> dimension = Optional.empty();
    private Optional<Double> y = Optional.empty();

    @Override
    public Optional<Component> getName() { return name; }
    @Override
    public void setName(Optional<Component> name) { this.name = name; }
    @Override
    public Optional<ResourceKey<Level>> getDimension() { return dimension; }
    @Override
    public void setDimension(Optional<ResourceKey<Level>> dimension) { this.dimension = dimension; }
    @Override
    public Optional<Double> getY() { return y; }
    @Override
    public void setY(Optional<Double> y) { this.y = y; }

    // TODO ? @WrapMethod for equals and hashCode to include name, dimension, and y in the comparison and hash calculation

    static {
        CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                MapDecorationType.CODEC.fieldOf("type").forGetter(d -> d.type()),
                Codec.DOUBLE.fieldOf("x").forGetter(d -> d.x()),                
                Codec.DOUBLE.optionalFieldOf("y").forGetter(d -> ((MapDecorationsEntryMixin)(Object)d).y),
                Codec.DOUBLE.fieldOf("z").forGetter(d -> d.z()),
                Codec.FLOAT.fieldOf("rotation").forGetter(d -> d.rotation()),
                ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(d -> ((MapDecorationsEntryMixin)(Object)d).name),
                Level.RESOURCE_KEY_CODEC.optionalFieldOf("dimension").forGetter(d -> ((MapDecorationsEntryMixin)(Object)d).dimension)
            ).apply(instance,
                (type, x, y, z, rotation, name, dimension) -> {
                    MapDecorations.Entry decoration = new MapDecorations.Entry(type, x, z, rotation);
                    ((MapDecorationsEntryMixin)(Object)decoration).name = name;
                    ((MapDecorationsEntryMixin)(Object)decoration).dimension = dimension;
                    ((MapDecorationsEntryMixin)(Object)decoration).y = y;
                    return decoration;
                })
        );
    }
}