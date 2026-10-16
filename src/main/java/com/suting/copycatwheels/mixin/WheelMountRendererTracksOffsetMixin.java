//这个mixin是为了兼容Tracks的WheelMountRenderer而写的，
//强兼性地在渲染时加上Tracks的位置偏移，运用神秘的反射（
package com.suting.copycatwheels.mixin;

import com.suting.copycatwheels.CopycatWheelsMod;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Method;

@Mixin(WheelMountRenderer.class)
public class WheelMountRendererTracksOffsetMixin {

    private static boolean copycatwheels$tracksChecked = false;
    private static Method copycatwheels$heightMethod = null;

    @Redirect(
            method = "renderSafe",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ryanhcode/offroad/content/blocks/wheel_mount/"
                            + "WheelMountBlockEntity;getLerpedExtension(F)D"
            ),
            require = 0
    )
    private double copycatwheels$addTracksHeight(final WheelMountBlockEntity be, final float partialTicks) {
        final double original = be.getLerpedExtension(partialTicks);
        copycatwheels$ensureTracksMethods();
        if (copycatwheels$heightMethod == null) return original;
        try {
            final double height = (double) copycatwheels$heightMethod.invoke(be, partialTicks);
            return height == 0.0 ? original : original + height;
        } catch (final Throwable t) {
            return original;
        }
    }

    private static void copycatwheels$ensureTracksMethods() {
        if (copycatwheels$tracksChecked) return;
        copycatwheels$tracksChecked = true;
        try {
            final Class<?> offsetAccess = Class.forName(
                    "dev.qwxon.tracks.mixin_interface.WheelMountOffsetAccess");
            copycatwheels$heightMethod = offsetAccess.getMethod(
                    "tracks$getLerpedHeightOffset", float.class);
            copycatwheels$heightMethod.setAccessible(true);
            CopycatWheelsMod.LOGGER.info("[CW-TRACKS-RENDER] resolved tracks height method");
        } catch (final Throwable t) {
            copycatwheels$heightMethod = null;
            //CopycatWheelsMod.LOGGER.info("[CW-TRACKS-RENDER] Create: Tracks not present, skip");
        }
    }
}