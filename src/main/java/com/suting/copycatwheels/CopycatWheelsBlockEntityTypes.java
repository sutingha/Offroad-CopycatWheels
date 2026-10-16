package com.suting.copycatwheels;

import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CopycatWheelsBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BE_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CopycatWheelsMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WheelMountBlockEntity>> COPYCAT_WHEEL_MOUNT =
            BE_TYPES.register("copycat_wheel_mount",
                    () -> BlockEntityType.Builder.of(
                            (pos, state) -> new WheelMountBlockEntity(
                                    CopycatWheelsBlockEntityTypes.COPYCAT_WHEEL_MOUNT.get(), pos, state),
                            CopycatWheelsBlocks.COPYCAT_WHEEL_MOUNT.get()
                    ).build(null));

    public static void register(final IEventBus modEventBus) {
        BE_TYPES.register(modEventBus);
    }
}