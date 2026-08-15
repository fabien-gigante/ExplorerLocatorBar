package com.fabien_gigante.explorer_locator_bar.mixin;

import com.fabien_gigante.explorer_locator_bar.ILodestoneTrackerExt;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import java.util.Optional;
import io.netty.buffer.ByteBuf;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.component.LodestoneTracker;

@Mixin(LodestoneTracker.class)
public abstract class LodestoneTrackerMixin implements ILodestoneTrackerExt {
    @Shadow @Final @Mutable
    public static Codec<LodestoneTracker> CODEC;   
    @Shadow @Final @Mutable
    public static StreamCodec<ByteBuf, LodestoneTracker> STREAM_CODEC;  
    @Shadow 
    public Optional<GlobalPos> target;

    private Optional<Long> seed = Optional.empty();

    @WrapMethod(method = "equals")
    private boolean equalsWithSeed(Object obj, Operation<Boolean> original) {
        return original.call(obj) && seed.equals(((LodestoneTrackerMixin)(Object)obj).seed);
    }

    @WrapMethod(method = "hashCode")
    private int hashCodeWithSeed(Operation<Integer> original) {
        return 31 * original.call() + seed.hashCode();
    }

    private static long nextSeed(long seed) { return seed * 25214903917L + 11L & 281474976710655L; }

    private long getEffectiveSeed() {
        if (seed.isPresent()) return seed.get();
        return target.isPresent() ? nextSeed(target.get().pos().asLong()) : 0L;
    }

    @Override
    public int getColor() {
        return ARGB.setBrightness((int)getEffectiveSeed() | 0xFF000000, 0.9f);
    }
 
    @Override
    public void cycleColor(LodestoneTracker previous) {
        if (target.isEmpty() || previous == null || previous.target().isEmpty()) return;
        if (!target.get().equals(previous.target().get())) return;
        this.seed = Optional.of(nextSeed(((LodestoneTrackerMixin)(Object)previous).getEffectiveSeed()));
    }

    private static LodestoneTracker createWithSeed(Optional<GlobalPos> target, boolean tracked, Optional<Long> seed) {
        LodestoneTracker tracker = new LodestoneTracker(target, tracked);
        ((LodestoneTrackerMixin)(Object) tracker).seed = seed;
        return tracker;
    }    

    static {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.optionalFieldOf("target").forGetter(LodestoneTracker::target), 
            Codec.BOOL.optionalFieldOf("tracked", true).forGetter(LodestoneTracker::tracked),
            Codec.LONG.optionalFieldOf("seed").forGetter(d -> ((LodestoneTrackerMixin)(Object)d).seed)
        ).apply(instance, LodestoneTrackerMixin::createWithSeed));
        STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC.apply(ByteBufCodecs::optional), LodestoneTracker::target,
            ByteBufCodecs.BOOL, LodestoneTracker::tracked,
            ByteBufCodecs.LONG.apply(ByteBufCodecs::optional), d -> ((LodestoneTrackerMixin)(Object)d).seed,
            LodestoneTrackerMixin::createWithSeed);
    }
}
