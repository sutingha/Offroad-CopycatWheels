package com.suting.copycatwheels.mixin;

import com.suting.copycatwheels.CopycatWheelAccess;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//槽位变化只对Be数据做清空（
@Mixin(WheelMountInventory.class)
public abstract class WheelMountInventoryMixin {

    @Shadow
    private WheelMountBlockEntity be;

    @Shadow
    public boolean suppressUpdate;

    @Inject(method = "setChanged", at = @At("HEAD"))
    private void copycatWheels$onStackChange(final CallbackInfo ci) {
        if (this.suppressUpdate) return;
        if (this.be == null) return;
        if (!(this.be instanceof final CopycatWheelAccess access)) return;

        if (access.copycatWheels$getCopycatBlock() != null) {
            access.copycatWheels$setCopycatBlock(null);
            access.copycatWheels$setCopycatRotation(null);
            access.copycatWheels$setCopycatScale(1.0f);
            access.copycatWheels$setCopycatOffset(0f, 0f, 0f);
        }
    }
}