package com.suting.copycatwheels.mixin;

import com.suting.copycatwheels.CopycatWheelAccess;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(WheelMountBlockEntity.class)
public abstract class WheelMountBlockEntityFrictionMixin {

    @Redirect(
            method = "sable$physicsTick",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ryanhcode/offroad/content/blocks/wheel_mount/"
                            + "WheelMountBlockEntity;fudgeFriction(D)D"
            ),
            require = 0
    )
    private double copycatWheels$modifyFriction(final double realValue) {
        final double base = WheelMountBlockEntity.fudgeFriction(realValue);
        final float mult = ((CopycatWheelAccess) (Object) this).copycatWheels$getFrictionMultiplier();
        return Math.max(0.0, Math.min(1.0, base * mult));
    }
}