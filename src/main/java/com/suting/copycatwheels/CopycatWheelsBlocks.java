package com.suting.copycatwheels;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CopycatWheelsBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CopycatWheelsMod.MODID);

    public static final DeferredBlock<CopycatWheelMountBlock> COPYCAT_WHEEL_MOUNT =
            BLOCKS.register("copycat_wheel_mount",
                    () -> new CopycatWheelMountBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_GRAY)
                                    .noOcclusion()
                                    .isRedstoneConductor((s, l, p) -> false)
                                    .sound(SoundType.STONE)
                                    .strength(2.0F)
                    ));

    public static void register(final IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}