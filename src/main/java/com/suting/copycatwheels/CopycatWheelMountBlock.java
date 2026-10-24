package com.suting.copycatwheels;

import com.simibubi.create.content.redstone.RoseQuartzLampBlock;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlock;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;

public class CopycatWheelMountBlock extends WheelMountBlock {

    public CopycatWheelMountBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends WheelMountBlockEntity> getBlockEntityType() {
        return CopycatWheelsBlockEntityTypes.COPYCAT_WHEEL_MOUNT.get();
    }

    private BlockState copycatWheels$getMaterial(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof WheelMountBlockEntity mount
                && mount instanceof CopycatWheelAccess access) {
            BlockState material = access.copycatWheels$getMaterial();
            if (material != null && !material.isAir()) {
                return material;
            }
        }
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockState material = copycatWheels$getMaterial(level, pos);
        return material != null ? material.getShape(level, pos, context) : super.getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockState material = copycatWheels$getMaterial(level, pos);
        return material != null ? material.getCollisionShape(level, pos, context) : super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState material = copycatWheels$getMaterial(level, pos);
        return material != null ? material.getOcclusionShape(level, pos) : super.getOcclusionShape(state, level, pos);
    }

    @Override
    public int getLightEmission(final BlockState state, final BlockGetter level, final BlockPos pos) {
        final AuxiliaryLightManager lm = level.getAuxLightManager(pos);
        return lm != null ? lm.getLightAt(pos) : super.getLightEmission(state, level, pos);
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
                if (existing.is(blockItem.getBlock())) {
                    if (!copycatWheels$cycleMaterial(existing, access)) {
                        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 0.95F);
                    return ItemInteractionResult.SUCCESS;
                }
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

    private static boolean copycatWheels$cycleMaterial(BlockState material, CopycatWheelAccess access) {
        if (material.hasProperty(TrapDoorBlock.HALF) && material.getOptionalValue(TrapDoorBlock.OPEN).orElse(false)) {
            access.copycatWheels$setMaterial(material.cycle(TrapDoorBlock.HALF));
        } else if (material.hasProperty(BlockStateProperties.FACING)) {
            access.copycatWheels$setMaterial(material.cycle(BlockStateProperties.FACING));
        } else if (material.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            access.copycatWheels$setMaterial(material.setValue(BlockStateProperties.HORIZONTAL_FACING,
                    material.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()));
        } else if (material.hasProperty(BlockStateProperties.AXIS)) {
            access.copycatWheels$setMaterial(material.cycle(BlockStateProperties.AXIS));
        } else if (material.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            access.copycatWheels$setMaterial(material.cycle(BlockStateProperties.HORIZONTAL_AXIS));
        } else if (material.hasProperty(BlockStateProperties.LIT)) {
            access.copycatWheels$setMaterial(material.cycle(BlockStateProperties.LIT));
        } else if (material.hasProperty(RoseQuartzLampBlock.POWERING)) {
            access.copycatWheels$setMaterial(material.cycle(RoseQuartzLampBlock.POWERING));
        } else {
            return false;
        }
        return true;
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
                    level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(material));
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