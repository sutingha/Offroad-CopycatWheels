package com.suting.copycatwheels.mixin;

import com.suting.copycatwheels.content.block.copycat_wheel_mount_ii.CopycatWheelMountIIBlockEntity;
import dev.qwxon.tracks.mixin_interface.WheelMountOffsetAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(CopycatWheelMountIIBlockEntity.class)
public abstract class CopycatWheelMountIIBlockEntityTracksMixin implements WheelMountOffsetAccess {

    @Override
    public double tracks$adjustLateralOffset(int dir) {
        return ((CopycatWheelMountIIBlockEntity)(Object)this).copycatWheels$adjustLateralOffset(dir);
    }

    @Override
    public double tracks$adjustLongitudinalOffset(int dir) {
        return ((CopycatWheelMountIIBlockEntity)(Object)this).copycatWheels$adjustLongitudinalOffset(dir);
    }

    @Override
    public double tracks$adjustHeightOffset(int dir) {
        return ((CopycatWheelMountIIBlockEntity)(Object)this).copycatWheels$adjustHeightOffset(dir);
    }

    @Override
    public double tracks$getLerpedLateralOffset(float pt) {
        return ((CopycatWheelMountIIBlockEntity)(Object)this).copycatWheels$getLerpedLateralOffset(pt);
    }

    @Override
    public double tracks$getLerpedLongitudinalOffset(float pt) {
        return ((CopycatWheelMountIIBlockEntity)(Object)this).copycatWheels$getLerpedLongitudinalOffset(pt);
    }

    @Override
    public double tracks$getLerpedHeightOffset(float pt) {
        return ((CopycatWheelMountIIBlockEntity)(Object)this).copycatWheels$getLerpedHeightOffset(pt);
    }

    @Override
    public double tracks$getLerpedYaw(float pt) {
        return ((CopycatWheelMountIIBlockEntity)(Object)this).copycatWheels$getLerpedYaw(pt);
    }

    @Override
    public boolean tracks$isVisualSuspensionHidden() {
        return ((CopycatWheelMountIIBlockEntity)(Object)this).copycatWheels$isVisualSuspensionHidden();
    }

    @Override
    public void tracks$toggleVisualSuspensionHidden() {
        ((CopycatWheelMountIIBlockEntity)(Object)this).copycatWheels$toggleVisualSuspensionHidden();
    }

    @Override
    public int tracks$getClientSteeringSignalLeft() {
        return ((CopycatWheelMountIIBlockEntity)(Object)this).copycatWheels$getClientSteeringSignalLeft();
    }

    @Override
    public int tracks$getClientSteeringSignalRight() {
        return ((CopycatWheelMountIIBlockEntity)(Object)this).copycatWheels$getClientSteeringSignalRight();
    }

    @Override
    public void tracks$resetTuning() {
        ((CopycatWheelMountIIBlockEntity)(Object)this).copycatWheels$resetTuning();
    }

    @Override
    public double tracks$adjustTuning(String key, int direction) {
        return ((CopycatWheelMountIIBlockEntity)(Object)this).copycatWheels$adjustTuning(key, direction);
    }

    @Override
    public double tracks$getTuning(String key) {
        return ((CopycatWheelMountIIBlockEntity)(Object)this).copycatWheels$getTuning(key);
    }
}