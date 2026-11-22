package com.suting.copycatwheels.content.block.copycat_wheel_mount_ii;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.suting.copycatwheels.content.block.copycat_wheel_mount.CopycatWheelMountBlock;
import com.suting.copycatwheels.core.CopycatBlockDrops;
import com.suting.copycatwheels.core.CopycatRenderState;
import com.suting.copycatwheels.core.CopycatWheelAccess;
import com.suting.copycatwheels.registry.CopycatWheelsBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;

public class CopycatWheelMountIIBlock extends HorizontalKineticBlock implements IBE<CopycatWheelMountIIBlockEntity>, IWrenchable {

    public static final BooleanProperty TRACKS_HIDDEN = BooleanProperty.create("tracks_hidden");
    private static final VoxelShape TRACKS_HIDDEN_SELECTION_SHAPE = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);

    public CopycatWheelMountIIBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TRACKS_HIDDEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TRACKS_HIDDEN);
    }

    @Override
    public Class<CopycatWheelMountIIBlockEntity> getBlockEntityClass() {
        return CopycatWheelMountIIBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CopycatWheelMountIIBlockEntity> getBlockEntityType() {
        return CopycatWheelsBlockEntityTypes.COPYCAT_WHEEL_MOUNT_II.get();
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction direction) {
        Direction facing = state.getValue(HORIZONTAL_FACING);
        return direction == facing || direction == facing.getOpposite();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        final AuxiliaryLightManager lm = level.getAuxLightManager(pos);
        if (lm != null) {
            final int aux = lm.getLightAt(pos);
            if (aux > 0) return aux;
        }
        if (level.getBlockEntity(pos) instanceof CopycatWheelAccess access) {
            final BlockState material = access.copycatWheels$getMaterial();
            if (material != null && !material.isAir()) {
                return material.getLightEmission(level, pos);
            }
        }
        return super.getLightEmission(state, level, pos);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(TRACKS_HIDDEN) ? RenderShape.INVISIBLE : super.getRenderShape(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(TRACKS_HIDDEN)) return TRACKS_HIDDEN_SELECTION_SHAPE;
        if (level.getBlockEntity(pos) instanceof CopycatWheelAccess access) {
            BlockState material = access.copycatWheels$getMaterial();
            if (material != null && !material.isAir()) return material.getShape(level, pos, context);
        }
        return super.getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(TRACKS_HIDDEN) && context != CollisionContext.empty()) return Shapes.empty();
        if (level.getBlockEntity(pos) instanceof CopycatWheelAccess access) {
            BlockState material = access.copycatWheels$getMaterial();
            if (material != null && !material.isAir()) return material.getCollisionShape(level, pos, context);
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.getValue(TRACKS_HIDDEN)) return Shapes.empty();
        if (level.getBlockEntity(pos) instanceof CopycatWheelAccess access) {
            BlockState material = access.copycatWheels$getMaterial();
            if (material != null && !material.isAir()) return material.getOcclusionShape(level, pos);
        }
        return super.getOcclusionShape(state, level, pos);
    }

    @Override
    public InteractionResult onSneakWrenched(final BlockState state, final UseOnContext context) {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        final Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        final BlockHitResult hit = new BlockHitResult(
                context.getClickLocation(), context.getClickedFace(), pos, false);

        final ItemInteractionResult crouch = useItemOn(
                context.getItemInHand(), state, level, pos, player, context.getHand(), hit);
        if (crouch == ItemInteractionResult.SUCCESS || crouch == ItemInteractionResult.CONSUME) {
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof final ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (player != null && !player.isCreative()) {
            final ItemStack drop = new ItemStack(state.getBlock());
            if (!player.getInventory().add(drop)) {
                player.drop(drop, false);
            }
        }

        state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);
        level.destroyBlock(pos, false);
        IWrenchable.playRemoveSound(level, pos);
        return InteractionResult.SUCCESS;
    }


    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction hitFace = context.getClickedFace();
        Direction facing = state.getValue(HORIZONTAL_FACING);

        if (level.getBlockEntity(pos) instanceof CopycatWheelMountIIBlockEntity be) {
            BlockState material = be.copycatWheels$getMaterial();

            if ((hitFace == facing.getClockWise() || hitFace == facing.getCounterClockWise()) && (material == null || material.isAir())) {
                return rotateWithWrench(state, context);
            }

            if (material != null && !material.isAir()) {
                if (!level.isClientSide) {
                    if (player == null || !player.hasInfiniteMaterials()) {
                        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(material.getBlock()));
                    }
                    be.copycatWheels$setMaterial(null);
                    level.playSound(null, pos, material.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
                    level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(material));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return rotateWithWrench(state, context);
    }

    private InteractionResult rotateWithWrench(final BlockState state, final UseOnContext context) {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();

        final BlockState rotated = getRotatedBlockState(state, context.getClickedFace());
        if (!rotated.canSurvive(level, pos)) {
            return InteractionResult.PASS;
        }

        KineticBlockEntity.switchToBlockState(level, pos, updateAfterWrenched(rotated, context));
        if (level.getBlockState(pos) != state) {
            IWrenchable.playRotateSound(level, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (player.isShiftKeyDown()) {
            Direction hitFace = hitResult.getDirection();
            if (hitFace == Direction.UP) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.cycle(TRACKS_HIDDEN), 2);
                    level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5F, state.getValue(TRACKS_HIDDEN) ? 0.8F : 1.2F);
                }
                return ItemInteractionResult.CONSUME;
            } else if (hitFace == state.getValue(HORIZONTAL_FACING)) {
                if (!level.isClientSide && level.getBlockEntity(pos) instanceof CopycatWheelMountIIBlockEntity be) {
                    be.copycatWheels$toggleVisualSuspensionHidden();
                    level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5F, 1.1F);
                }
                return ItemInteractionResult.CONSUME;
            }
        }

        if (level.getBlockEntity(pos) instanceof CopycatWheelMountIIBlockEntity be) {
            Direction facing = state.getValue(HORIZONTAL_FACING);
            Direction leftSide = facing.getCounterClockWise();
            Direction rightSide = facing.getClockWise();
            Direction hitFace = hitResult.getDirection();

            if (hitFace == leftSide || hitFace == rightSide) {
                if (stack.getItem() instanceof BlockItem blockItem) {
                    ItemStack wheel = hitFace == leftSide ? be.getLeftWheel() : be.getRightWheel();
                    if (!wheel.isEmpty() && be.isCopycatWheel(wheel)) {
                        if (!level.isClientSide) {
                            be.copycatWheels$setCopycatBlock(BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()));
                            if (!player.hasInfiniteMaterials()) stack.shrink(1);
                            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS, 0.75F, 1.0F);
                        }
                        return ItemInteractionResult.SUCCESS;
                    }
                }
                return installOrRemoveWheel(stack, level, pos, player, be, hitFace == leftSide);
            }

            return handleCopycatLogic(stack, state, level, pos, player, be);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private ItemInteractionResult installOrRemoveWheel(ItemStack stack, Level level, BlockPos pos, Player player, CopycatWheelMountIIBlockEntity be, boolean isLeft) {
        if (stack.isEmpty()) {
            ItemStack removed = isLeft ? be.getLeftWheel().copy() : be.getRightWheel().copy();
            if (removed.isEmpty()) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }

            if (!level.isClientSide) {
                boolean wasCopycat = be.isCopycatWheel(removed);
                ResourceLocation sideBlock = isLeft ? be.getLeftCopycatBlock() : be.getRightCopycatBlock();

                if (isLeft) be.getInventory().setLeftWheel(ItemStack.EMPTY);
                else be.getInventory().setRightWheel(ItemStack.EMPTY);

                if (!player.hasInfiniteMaterials()) {
                    if (!player.getInventory().add(removed)) player.drop(removed, false);
                }

                if (wasCopycat && sideBlock != null) {
                    CopycatBlockDrops.dropCopycatBlock(level, pos, sideBlock, player);
                    if (isLeft) be.setLeftCopycatBlock(null);
                    else be.setRightCopycatBlock(null);
                }

                if (!be.isCopycatWheel(be.getLeftWheel()) && !be.isCopycatWheel(be.getRightWheel())) {
                    be.copycatWheels$setCopycatBlock(null);
                }

                be.setChanged();
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.PLAYERS, 0.75F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        } else if (stack.get(OffroadDataComponents.TIRE) != null) {
            if (!level.isClientSide) {
                ItemStack old = isLeft ? be.getLeftWheel().copy() : be.getRightWheel().copy();
                boolean occupied = !old.isEmpty();
                boolean wasCopycat = occupied && be.isCopycatWheel(old);
                ResourceLocation sideBlock = isLeft ? be.getLeftCopycatBlock() : be.getRightCopycatBlock();

                ItemStack wheel = stack.copyWithCount(1);
                if (isLeft) be.getInventory().setLeftWheel(wheel);
                else be.getInventory().setRightWheel(wheel);

                if (!player.hasInfiniteMaterials()) {
                    stack.shrink(1);
                    if (occupied) {
                        if (!player.getInventory().add(old)) player.drop(old, false);
                    }
                }

                boolean newIsCopycat = be.isCopycatWheel(wheel);
                if (wasCopycat && !newIsCopycat && sideBlock != null) {
                    CopycatBlockDrops.dropCopycatBlock(level, pos, sideBlock, player);
                    if (isLeft) be.setLeftCopycatBlock(null);
                    else be.setRightCopycatBlock(null);
                }

                if (!be.isCopycatWheel(be.getLeftWheel()) && !be.isCopycatWheel(be.getRightWheel())) {
                    be.copycatWheels$setCopycatBlock(null);
                }

                be.setChanged();
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS, 0.75F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

    private ItemInteractionResult handleCopycatLogic(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, CopycatWheelMountIIBlockEntity be) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        BlockState existing = be.copycatWheels$getMaterial();
        if (existing != null && !existing.isAir()) {
            if (existing.is(blockItem.getBlock())) {
                if (CopycatWheelMountBlock.copycatWheels$cycleMaterial(existing, be)) {
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 0.95F);
                    return ItemInteractionResult.SUCCESS;
                }
            }
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        Block block = blockItem.getBlock();
        if (block instanceof EntityBlock) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        BlockState newMaterial = block.defaultBlockState();
        VoxelShape shape = newMaterial.getShape(level, pos);
        if (shape.isEmpty() || !shape.bounds().equals(Shapes.block().bounds())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        VoxelShape collision = newMaterial.getCollisionShape(level, pos);
        if (collision.isEmpty() || !collision.bounds().equals(Shapes.block().bounds())) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        be.copycatWheels$setMaterial(newMaterial);
        if (!player.hasInfiniteMaterials()) stack.shrink(1);
        level.playSound(null, pos, newMaterial.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.95F);

        if (CopycatRenderState.camoHintEnabled) {
            player.displayClientMessage(Component.translatable("copycatwheels.msg.copycat_set", BuiltInRegistries.BLOCK.getKey(block).toString()), true);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && !state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof CopycatWheelMountIIBlockEntity be) {
                if (!level.isClientSide) {
                    for (ItemStack wheel : new ItemStack[]{be.getLeftWheel(), be.getRightWheel()}) {
                        if (!wheel.isEmpty()) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, wheel.copy());
                    }

                    BlockState material = be.copycatWheels$getMaterial();
                    if (material != null && !material.isAir()) {
                        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                new ItemStack(material.getBlock()));
                    }
                    ResourceLocation leftCb = be.getLeftCopycatBlock();
                    ResourceLocation rightCb = be.getRightCopycatBlock();
                    if (leftCb != null) {
                        CopycatBlockDrops.dropCopycatBlock(level, pos, leftCb, null);
                    }
                    if (rightCb != null && !rightCb.equals(leftCb)) {
                        CopycatBlockDrops.dropCopycatBlock(level, pos, rightCb, null);
                    }
                }
                be.clearContent();
            }
            level.removeBlockEntity(pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
