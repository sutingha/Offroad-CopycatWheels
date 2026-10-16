package com.suting.copycatwheels.client;

import com.suting.copycatwheels.CopycatWheelAccess;
import com.suting.copycatwheels.CopycatWheelsBlocks;
import com.suting.copycatwheels.CopycatWheelsMod;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class CopycatWheelsClientColors {

    private CopycatWheelsClientColors() {}

    public static void register() {
        Minecraft.getInstance().getBlockColors().register(
                new WrappedBlockColor(),
                CopycatWheelsBlocks.COPYCAT_WHEEL_MOUNT.get());
        //CopycatWheelsMod.LOGGER.info("[CW-COLOR] registered wrapped block color for copycat_wheel_mount");
    }

    private static class WrappedBlockColor implements BlockColor {
        @Override
        public int getColor(final BlockState state, @Nullable final BlockAndTintGetter level,
                            @Nullable final BlockPos pos, final int tintIndex) {
            if (level == null || pos == null) return 0xFFFFFFFF;
            try {
                if (level.getBlockEntity(pos) instanceof final WheelMountBlockEntity mount
                        && mount instanceof final CopycatWheelAccess access) {
                    final BlockState material = access.copycatWheels$getMaterial();
                    if (material != null && !material.isAir()) {
                        return Minecraft.getInstance().getBlockColors()
                                .getColor(material, level, pos, tintIndex);
                    }
                }
            } catch (final Throwable ignored) {
                //在Sable子关卡可能会抛异常
                //棍母
            }
            return 0xFFFFFFFF;
        }
    }
}