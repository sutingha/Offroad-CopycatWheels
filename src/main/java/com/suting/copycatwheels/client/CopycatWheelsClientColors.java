package com.suting.copycatwheels.client;

import com.suting.copycatwheels.CopycatWheelsMod;
import com.suting.copycatwheels.core.CopycatWheelAccess;
import com.suting.copycatwheels.registry.CopycatWheelsBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class CopycatWheelsClientColors {
    private CopycatWheelsClientColors() {}

    public static void register() {
        BlockColor color = new WrappedBlockColor();
        Minecraft.getInstance().getBlockColors().register(
                color,
                CopycatWheelsBlocks.COPYCAT_WHEEL_MOUNT.get(),
                CopycatWheelsBlocks.COPYCAT_WHEEL_MOUNT_II.get() 
        );
        CopycatWheelsMod.LOGGER.info("[CW-COLOR] registered wrapped block color for I & II");
    }

    private static class WrappedBlockColor implements BlockColor {
        @Override
        public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
            if (level == null || pos == null) return 0xFFFFFFFF;
            try {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof CopycatWheelAccess access) {
                    BlockState material = access.copycatWheels$getMaterial();
                    if (material != null && !material.isAir()) {
                        return Minecraft.getInstance().getBlockColors().getColor(material, level, pos, tintIndex);
                    }
                }
            } catch (Throwable ignored) {}
            return 0xFFFFFFFF;
        }
    }
}