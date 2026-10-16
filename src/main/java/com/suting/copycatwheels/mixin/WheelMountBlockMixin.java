package com.suting.copycatwheels.mixin;

import com.suting.copycatwheels.CopycatBlockDrops;
import com.suting.copycatwheels.CopycatConfig;
import com.suting.copycatwheels.CopycatWheelAccess;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlock;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.suting.copycatwheels.CopycatRenderState;

@Mixin(WheelMountBlock.class)
public class WheelMountBlockMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void copycatWheels$preSwitchStacks(
            final ItemStack stack,
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final Player player,
            final InteractionHand hand,
            final BlockHitResult hitResult,
            final CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        if (player.isShiftKeyDown()) return;
        if (level.isClientSide) return;

        final Direction hitDirection = hitResult.getDirection();
        final Direction facing = state.getValue(WheelMountBlock.HORIZONTAL_FACING);
        if (!hitDirection.equals(facing) && hitDirection != Direction.DOWN) return;

        if (!(level.getBlockEntity(pos) instanceof final WheelMountBlockEntity mount)) return;
        if (!(mount instanceof final CopycatWheelAccess access)) return;

        final ResourceLocation copycat = access.copycatWheels$getCopycatBlock();
        if (copycat == null) return;

        final TireLike tireLike = stack.get(OffroadDataComponents.TIRE);
        if (!(stack.isEmpty() || tireLike != null)) return;

        CopycatBlockDrops.dropCopycatBlock(level, pos, copycat, player);
        access.copycatWheels$setCopycatBlock(null);
        access.copycatWheels$setCopycatRotation(null);
        access.copycatWheels$setCopycatScale(1.0f);
        access.copycatWheels$setCopycatOffset(0f, 0f, 0f);
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void copycatWheels$useItemOn(
            final ItemStack stack,
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final Player player,
            final InteractionHand hand,
            final BlockHitResult hitResult,
            final CallbackInfoReturnable<ItemInteractionResult> cir
    ) {
        if (player.isShiftKeyDown()) return;

        if (!(stack.getItem() instanceof final BlockItem blockItem)) return;

        final Direction hitFace = hitResult.getDirection();
        final Direction facing = state.getValue(WheelMountBlock.HORIZONTAL_FACING);
        if (!hitFace.equals(facing) && hitFace != Direction.DOWN) return;

        if (!(level.getBlockEntity(pos) instanceof final WheelMountBlockEntity mount)) return;

        final ItemStack held = mount.getHeldItem();
        if (held.isEmpty()) return;

        if (!copycatWheels$isCopycatWheel(held)) return;

        if (!(mount instanceof final CopycatWheelAccess access)) return;

        if (level.isClientSide) {
            cir.setReturnValue(ItemInteractionResult.SUCCESS);
            return;
        }

        final ResourceLocation oldCopycat = access.copycatWheels$getCopycatBlock();
        if (oldCopycat != null) {
            CopycatBlockDrops.dropCopycatBlock(level, pos, oldCopycat, player);
        }

        final ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
        access.copycatWheels$setCopycatBlock(blockId);
        access.copycatWheels$setCopycatRotation(null);
        access.copycatWheels$setCopycatScale(1.0f);
        access.copycatWheels$setCopycatOffset(0f, 0f, 0f);

        if (!player.hasInfiniteMaterials()) stack.shrink(1);

        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM,
                SoundSource.PLAYERS, 0.75f, 1.0f);
        if (CopycatRenderState.camoHintEnabled) {
            player.displayClientMessage(
                    Component.translatable("copycatwheels.msg.copycat_set", blockId.toString()), true);
        }
        cir.setReturnValue(ItemInteractionResult.SUCCESS);
    }

    @Inject(method = "onRemove", at = @At("HEAD"))
    private void copycatWheels$onRemove(
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final BlockState newState,
            final boolean isMoving,
            final CallbackInfo ci
    ) {
        if (state.getBlock() == newState.getBlock()) return;
        if (level.isClientSide) return;

        final BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof final CopycatWheelAccess access)) return;

        final ResourceLocation copycat = access.copycatWheels$getCopycatBlock();
        if (copycat == null) return;

        CopycatBlockDrops.dropCopycatBlock(level, pos, copycat, null);
    }

    private static boolean copycatWheels$isCopycatWheel(final ItemStack stack) {
        final ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return "offroad".equals(id.getNamespace()) && id.getPath().startsWith("copycat_wheel");
    }
}