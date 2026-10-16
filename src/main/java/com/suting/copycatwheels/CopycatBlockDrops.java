package com.suting.copycatwheels;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class CopycatBlockDrops {

    private CopycatBlockDrops() {}


    public static void dropCopycatBlock(
            final Level level,
            final BlockPos pos,
            final ResourceLocation blockId,
            @Nullable final Player player
    ) {
        if (level.isClientSide || blockId == null) return;
        if (player != null && player.hasInfiniteMaterials()) return;

        final Block block = BuiltInRegistries.BLOCK.get(blockId);
        final ItemStack stack = new ItemStack(block);
        if (stack.isEmpty()) return;

        Containers.dropItemStack(level,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                stack);
    }
}