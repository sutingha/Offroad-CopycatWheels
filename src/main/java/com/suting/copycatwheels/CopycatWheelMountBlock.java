package com.suting.copycatwheels;

import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlock;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CopycatWheelMountBlock extends WheelMountBlock {

    public CopycatWheelMountBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends WheelMountBlockEntity> getBlockEntityType() {
        return CopycatWheelsBlockEntityTypes.COPYCAT_WHEEL_MOUNT.get();
    }

    @Override
    public boolean hasDynamicLightEmission(final BlockState state) {
        return true; // 声明本方块具有动态光照，光照引擎会不断查询 getLightEmission
    }

    @Override
    public int getLightEmission(final BlockState state, final BlockGetter level, final BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof final WheelMountBlockEntity be
                && be instanceof final CopycatWheelAccess access) {
            final BlockState material = access.copycatWheels$getMaterial();
            if (material != null && !material.isAir()) {
                return material.getLightEmission(level, pos);
            }
        }
        return super.getLightEmission(state, level, pos);
    }

    @Override
    public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        final Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) return super.onWrenched(state, context);

        final Direction hitFace = context.getClickedFace();
        final Direction facing = state.getValue(WheelMountBlock.HORIZONTAL_FACING);

        if (hitFace.equals(facing) || hitFace == Direction.DOWN) {
            return super.onWrenched(state, context);
        }

        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();

        if (level.getBlockEntity(pos) instanceof final WheelMountBlockEntity mount
                && mount instanceof final CopycatWheelAccess access) {
            final BlockState material = access.copycatWheels$getMaterial();
            if (material != null && !material.isAir()) {
                if (!level.isClientSide) {
                    if (player == null || !player.hasInfiniteMaterials()) {
                        Containers.dropItemStack(level,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                new ItemStack(material.getBlock()));
                    }
                    access.copycatWheels$setMaterial(null);
                    level.playSound(null, pos, material.getSoundType().getBreakSound(),
                            SoundSource.BLOCKS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
                    level.levelEvent(net.minecraft.world.level.block.LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(material));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.onWrenched(state, context);
    }

    @Override
    public BlockState playerWillDestroy(final Level level, final BlockPos pos,
                                        final BlockState state, final Player player) {
        if (!level.isClientSide && player != null && player.hasInfiniteMaterials()
                && level.getBlockEntity(pos) instanceof final CopycatWheelAccess access) {
            access.copycatWheels$setMaterial(null);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            final ItemStack stack,
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final Player player,
            final InteractionHand hand,
            final BlockHitResult hitResult
    ) {
        if (player.isShiftKeyDown()) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        if (stack.getItem() instanceof final BlockItem blockItem) {
            final Direction hitFace = hitResult.getDirection();
            final Direction facing = state.getValue(WheelMountBlock.HORIZONTAL_FACING);

            if (hitFace.equals(facing) || hitFace == Direction.DOWN) {
                return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
            }

            if (!(level.getBlockEntity(pos) instanceof final WheelMountBlockEntity mount)) {
                return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
            }
            if (!(mount instanceof final CopycatWheelAccess access)) {
                return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
            }

            final BlockState existing = access.copycatWheels$getMaterial();
            if (existing != null && !existing.isAir()) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }

            final Block block = blockItem.getBlock();
            if (block instanceof EntityBlock) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            final BlockState newMaterial = block.defaultBlockState();
            final VoxelShape shape = newMaterial.getShape(level, pos);
            if (shape.isEmpty() || !shape.bounds().equals(Shapes.block().bounds())) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            final VoxelShape collision = newMaterial.getCollisionShape(level, pos);
            if (collision.isEmpty() || !collision.bounds().equals(Shapes.block().bounds())) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }

            access.copycatWheels$setMaterial(newMaterial);
            if (!player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
            level.playSound(null, pos, newMaterial.getSoundType().getPlaceSound(),
                    SoundSource.BLOCKS, 1.0F, 0.95F);
            
            if (CopycatRenderState.camoHintEnabled) {
                player.displayClientMessage(
                        Component.translatable("copycatwheels.msg.copycat_set",
                                BuiltInRegistries.BLOCK.getKey(block).toString()),
                        true);
            }
            return ItemInteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public void onRemove(
            final BlockState state,
            final Level level,
            final BlockPos pos,
            final BlockState newState,
            final boolean isMoving
    ) {
        if (state.getBlock() != newState.getBlock()
                && level.getBlockEntity(pos) instanceof final CopycatWheelAccess access) {
            final BlockState material = access.copycatWheels$getMaterial();
            if (material != null && !material.isAir() && !level.isClientSide) {
                Containers.dropItemStack(level,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        new ItemStack(material.getBlock()));
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}