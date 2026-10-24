package com.suting.copycatwheels;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlock;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountInventory;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class CopycatWheelsCommands {

    private static final ResourceLocation GUNMU_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath("copycatwheels", "gunmu");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("copycat")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("config")
                .then(Commands.literal("get")
                    .then(Commands.literal("roll")
                        .executes(context -> {
                            double value = CopycatConfig.ROLL.get();
                            context.getSource().sendSuccess(
                                () -> Component.literal("Roll = " + value), false);
                            return 1;
                        })))
                .then(Commands.literal("set")
                    .then(Commands.literal("roll")
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0, 100))
                            .executes(context -> {
                                double value = DoubleArgumentType.getDouble(context, "value");
                                CopycatConfig.ROLL.set(value);
                                //CopycatConfig.SPEC.save();
                                CopycatConfig.SERVER_SPEC.save();
                                context.getSource().sendSuccess(
                                    () -> Component.literal("Roll updated to " + value), false);
                                return 1;
                            }))))));

        dispatcher.register(Commands.literal("copycatwheels")
            .then(Commands.literal("debug")
                .requires(source -> {
                    final ServerPlayer p = source.getPlayer();
                    return p != null && copycatWheels$holdingDebugger(p);
                })
                .then(Commands.argument("x", IntegerArgumentType.integer())
                    .then(Commands.argument("y", IntegerArgumentType.integer())
                        .then(Commands.argument("z", IntegerArgumentType.integer())
                            .then(Commands.argument("mode", IntegerArgumentType.integer(0, 6))
                                .then(Commands.argument("face", StringArgumentType.word())
                                    .then(Commands.argument("delta", FloatArgumentType.floatArg())
                                        .executes(ctx -> copycatWheels$runDebug(
                                            ctx.getSource(),
                                            IntegerArgumentType.getInteger(ctx, "x"),
                                            IntegerArgumentType.getInteger(ctx, "y"),
                                            IntegerArgumentType.getInteger(ctx, "z"),
                                            IntegerArgumentType.getInteger(ctx, "mode"),
                                            StringArgumentType.getString(ctx, "face"),
                                            FloatArgumentType.getFloat(ctx, "delta")
                                        )))))))))
            .then(Commands.literal("render")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("base")
                    .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                            CopycatRenderState.renderBaseChannel = enabled;
                            context.getSource().sendSuccess(
                                () -> Component.literal("Base channel: " + (enabled ? "ON" : "OFF")), false);
                            return 1;
                        })))
                .then(Commands.literal("override")
                    .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                            CopycatRenderState.renderOverrideChannel = enabled;
                            context.getSource().sendSuccess(
                                () -> Component.literal("Override channel: " + (enabled ? "ON" : "OFF")), false);
                            return 1;
                        }))))
            .then(Commands.literal("gunmu")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                    .executes(context -> {
                        boolean enabled = BoolArgumentType.getBool(context, "enabled");
                        CopycatRenderState.gunmuMode = enabled;
                        context.getSource().sendSuccess(
                            () -> Component.literal("Gunmu mode: " + (enabled ? "ON" : "OFF")), false);

                        if (enabled) {
                            final ServerPlayer p = context.getSource().getPlayer();
                            if (p != null) copycatWheels$grantGunmu(p);
                        }
                        return 1;
                    })))
        );
    }

    private static void copycatWheels$grantGunmu(final ServerPlayer player) {
        final AdvancementHolder adv = player.server.getAdvancements().get(GUNMU_ADVANCEMENT);
        if (adv != null) {
            player.getAdvancements().award(adv, "0");
        } else {
            //仅供调试报错
            //CopycatWheelsMod.LOGGER.warn("[CW-ADV] advancement {} not found", GUNMU_ADVANCEMENT);
        }
    }

    /**
    CopycatConfig.HIDE_ACTION_MESSAGES来决定是否隐藏**/ 
    private static void copycatWheels$msg(final ServerPlayer player, final Component message) {
        if (CopycatConfig.HIDE_ACTION_MESSAGES.get()) return;
        player.displayClientMessage(message, true);
    }

    private static int copycatWheels$runDebug(
            final CommandSourceStack source,
            final int x, final int y, final int z,
            final int mode, final String faceStr, final float delta
    ) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final ServerLevel level = player.serverLevel();
        final BlockPos pos = new BlockPos(x, y, z);

        if (!(level.getBlockEntity(pos) instanceof final WheelMountBlockEntity mount)) return 0;
        if (!(mount instanceof final CopycatWheelAccess access)) return 0;

        //拆除
        if (mode == 0) {
            if (access.copycatWheels$getCopycatBlock() == null) {
                copycatWheels$msg(player,
                        Component.translatable("copycatwheels.msg.no_copycat"));
                return 1;
            }
            CopycatBlockDrops.dropCopycatBlock(level, pos, access.copycatWheels$getCopycatBlock(), player);

            access.copycatWheels$setCopycatBlock(null);
            access.copycatWheels$setCopycatRotation(null);
            access.copycatWheels$setCopycatScale(1.0f);
            access.copycatWheels$setCopycatOffset(0f, 0f, 0f);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                    SoundSource.PLAYERS, 0.75f, 1.0f);
            copycatWheels$msg(player,
                    Component.translatable("copycatwheels.msg.removed"));
            return 1;
        }

        //模式6：重置
        if (mode == 6) {
            access.copycatWheels$setCopycatRotation(null);
            access.copycatWheels$setCopycatScale(1.0f);
            access.copycatWheels$setFrictionMultiplier(1.0f);
            access.copycatWheels$setCopycatOffset(0f, 0f, 0f);

            final ItemStack stack = mount.getHeldItem();
            final TireLike old = stack.get(OffroadDataComponents.TIRE);
            if (old != null) {
                final ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                final float defaultRadius = copycatWheels$defaultRadiusFor(itemId);
                if (Math.abs(old.radius() - defaultRadius) > 1e-4) {
                    final TireLike restored = new TireLike(
                            defaultRadius, old.rotation(), old.offset(), old.model().orElse(null));
                    final ItemStack newStack = stack.copy();
                    newStack.set(OffroadDataComponents.TIRE, restored);

                    final WheelMountInventory inv = (WheelMountInventory) mount.getInventory();
                    final boolean oldSuppress = inv.suppressUpdate;
                    inv.suppressUpdate = true;
                    try { inv.slot.setStack(newStack); }
                    finally { inv.suppressUpdate = oldSuppress; }
                    mount.setChanged();
                    mount.sendData();
                }
            }

            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                    SoundSource.PLAYERS, 0.75f, 1.5f);
            copycatWheels$msg(player,
                    Component.translatable("copycatwheels.msg.reset"));
            return 1;
        }

        //其余模式需要轮架上有伪装模型
        if (access.copycatWheels$getCopycatBlock() == null) {
            copycatWheels$msg(player,
                    Component.translatable("copycatwheels.msg.no_copycat"));
            return 1;
        }

        //旋转
        if (mode == 1) {
            final Direction hitFace = Direction.byName(faceStr);
            if (hitFace == null) return 0;

            final BlockState state = mount.getBlockState();
            final Direction facing = state.getValue(WheelMountBlock.HORIZONTAL_FACING);
            final Direction dir = facing.getOpposite();
            final float yRot = switch (dir) {
                case NORTH -> 180f;
                case EAST  -> 90f;
                case SOUTH -> 0f;
                case WEST  -> -90f;
                default    -> 0f;
            };

            final Vec3 worldN = new Vec3(hitFace.getStepX(), hitFace.getStepY(), hitFace.getStepZ());
            final Vec3 localN = worldN.yRot((float) -Math.toRadians(yRot));

            final Quaternionf qDelta = new Quaternionf().rotateAxis(
                    (float) Math.toRadians(delta),
                    (float) localN.x, (float) localN.y, (float) localN.z);

            final float[] existing = access.copycatWheels$getCopycatRotation();
            final Quaternionf q0 = (existing == null)
                    ? new Quaternionf()
                    : new Quaternionf(existing[0], existing[1], existing[2], existing[3]);

            q0.premul(qDelta);

            access.copycatWheels$setCopycatRotation(
                    new float[]{q0.x(), q0.y(), q0.z(), q0.w()});

            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM,
                    SoundSource.PLAYERS, 0.75f, 1.0f);
            copycatWheels$msg(player,
                    Component.translatable("copycatwheels.msg.rotated"));
            return 1;
        }

        //缩放（是伪装模型的，与碰撞无关）
        if (mode == 2) {
            final float cur = access.copycatWheels$getCopycatScale();
            final float next = Mth.clamp(cur + delta, 0.1f, 10.0f);
            access.copycatWheels$setCopycatScale(next);

            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM,
                    SoundSource.PLAYERS, 0.6f, next);
            copycatWheels$msg(player,
                    Component.translatable("copycatwheels.msg.model_scale",
                            String.format("%.2f", next)));
            return 1;
        }

        //模式3碰撞半径
        if (mode == 3) {
            final ItemStack stack = mount.getHeldItem();
            final TireLike old = stack.get(OffroadDataComponents.TIRE);
            if (old == null) return 0;

            final float newRadius = Mth.clamp(old.radius() + delta, 0.05f, 5.0f);
            final TireLike newTire = new TireLike(
                    newRadius, old.rotation(), old.offset(), old.model().orElse(null));

            final ItemStack newStack = stack.copy();
            newStack.set(OffroadDataComponents.TIRE, newTire);

            final WheelMountInventory inv = (WheelMountInventory) mount.getInventory();
            final boolean oldSuppress = inv.suppressUpdate;
            inv.suppressUpdate = true;
            try { inv.slot.setStack(newStack); }
            finally { inv.suppressUpdate = oldSuppress; }
            mount.setChanged();
            mount.sendData();

            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM,
                    SoundSource.PLAYERS, 0.6f, newRadius * 2f);
            copycatWheels$msg(player,
                    Component.translatable("copycatwheels.msg.tire_radius",
                            String.format("%.3f", newRadius)));
            return 1;
        }

        //模式4：摩擦力倍率
        if (mode == 4) {
            final float cur = access.copycatWheels$getFrictionMultiplier();
            final float next = Mth.clamp(cur + delta, 0.0f, 2.0f);
            access.copycatWheels$setFrictionMultiplier(next);

            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM,
                    SoundSource.PLAYERS, 0.6f, 1.0f + next);
            copycatWheels$msg(player,
                    Component.translatable("copycatwheels.msg.friction",
                            String.format("%.2f", next)));
            return 1;
        }

        //模式5位置
        if (mode == 5) {
            final Direction hitFace = Direction.byName(faceStr);
            if (hitFace == null) return 0;

            final float dx = hitFace.getStepX() * delta;
            final float dy = hitFace.getStepY() * delta;
            final float dz = hitFace.getStepZ() * delta;

            final float nx = access.copycatWheels$getOffsetX() + dx;
            final float ny = access.copycatWheels$getOffsetY() + dy;
            final float nz = access.copycatWheels$getOffsetZ() + dz;

            access.copycatWheels$setCopycatOffset(nx, ny, nz);

            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM,
                    SoundSource.PLAYERS, 0.6f, 1.0f);
            copycatWheels$msg(player,
                    Component.translatable("copycatwheels.msg.offset",
                            String.format("%.2f", nx),
                            String.format("%.2f", ny),
                            String.format("%.2f", nz)));
            return 1;
        }

        return 0;
    }

    private static float copycatWheels$defaultRadiusFor(final ResourceLocation itemId) {
        if (itemId == null) return 1.0f;
        if ("copycat_wheel".equals(itemId.getPath())) {
            return TireLike.TIRE.radius();
        }
        return 1.0f;
    }

    private static boolean copycatWheels$holdingDebugger(final ServerPlayer player) {
        return copycatWheels$isDebugger(player.getMainHandItem())
                || copycatWheels$isDebugger(player.getOffhandItem());
    }

    private static boolean copycatWheels$isDebugger(final ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (CopycatItems.COPYCAT_REMOVER == null) return false;
        return stack.is(CopycatItems.COPYCAT_REMOVER.get());
    }
}