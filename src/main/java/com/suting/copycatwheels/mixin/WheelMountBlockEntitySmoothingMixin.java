package com.suting.copycatwheels.mixin;

import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WheelMountBlockEntity.class)
public class WheelMountBlockEntitySmoothingMixin {

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Mth;lerp(DDD)D",
                    ordinal = 2
            )
    )
    private double copycatWheels$snapExtension(final double delta, final double start, final double end) {
        return Mth.lerp(0.95, start, end);
    }
}