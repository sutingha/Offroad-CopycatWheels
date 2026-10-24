package com.suting.copycatwheels.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.suting.copycatwheels.CopycatRenderState;
import com.suting.copycatwheels.CopycatWheelAccess;
import com.suting.copycatwheels.CopycatWheelsMod;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = CopycatWheelsMod.MODID, value = Dist.CLIENT)
public class CopycatWheelsClientRenderEvents {

    private static final AtomicInteger copycatWheels$frameCounter = new AtomicInteger(0);
    private static final Set<BlockPos> copycatWheels$loggedPositions = ConcurrentHashMap.newKeySet();

    private static Method copycatWheels$lerpedYawMethod = null;
    private static boolean copycatWheels$lerpedYawFailed = false;

    private static boolean copycatWheels$tracksChecked = false;
    private static Method copycatWheels$tracksLateralMethod = null;
    private static Method copycatWheels$tracksLongitudinalMethod = null;
    private static Method copycatWheels$tracksHeightMethod = null;

    private static final Map<ResourceLocation, Boolean> copycatWheels$overrideCache = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, BaseTransform> copycatWheels$baseTransforms = Map.of(
            ResourceLocation.fromNamespaceAndPath("create", "water_wheel"),
            new BaseTransform(Axis.XP, 90f, 0f, 0f, 0f),
            ResourceLocation.fromNamespaceAndPath("create", "large_water_wheel"),
            new BaseTransform(Axis.XP, 90f, 0f, 0f, 0f)
    );

    private record BaseTransform(Axis axis, float degrees, float tx, float ty, float tz) {}

    private static final Map<ResourceLocation, ResourceLocation> copycatWheels$aliases = Map.of(
            ResourceLocation.fromNamespaceAndPath("create", "copper_valve_handle"),
            ResourceLocation.fromNamespaceAndPath("create", "valve_handle"),
            ResourceLocation.fromNamespaceAndPath("create", "copper_water_wheel"),
            ResourceLocation.fromNamespaceAndPath("create", "water_wheel")
    );

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        Vec3 camPos = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        PoseStack ms = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

        int cx = ((int) camPos.x) >> 4;
        int cz = ((int) camPos.z) >> 4;
        int radius = 8;
        int found = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                LevelChunk chunk;
                try { chunk = level.getChunk(cx + dx, cz + dz); }
                catch (Exception e) { continue; }
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof WheelMountBlockEntity mount) {
                        if (copycatWheels$renderOne(mount, null, ms, buffer, dispatcher, level, camPos, partialTick)) found++;
                    }
                }
            }
        }

        ClientSubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container != null) {
            for (SubLevel sub : container.getAllSubLevels()) {
                LevelPlot plot = sub.getPlot();
                if (plot == null) continue;

                Pose3dc pose;
                if (sub instanceof ClientSubLevel clientSub) pose = clientSub.renderPose(partialTick);
                else pose = sub.logicalPose();

                for (BlockEntitySubLevelActor actor : plot.getBlockEntityActors()) {
                    if (actor instanceof WheelMountBlockEntity mount) {
                        if (copycatWheels$renderOne(mount, pose, ms, buffer, dispatcher, level, camPos, partialTick)) found++;
                    }
                }
            }
        }

        buffer.endBatch();

        if (found > 0) {
            int n = copycatWheels$frameCounter.incrementAndGet();
            if (n <= 3 || n % 300 == 0) {
                CopycatWheelsMod.LOGGER.info(
                        "[CopycatWheels] ClientRenderEvents: rendered {} copycat blocks (frame #{})",
                        found, n);
            }
        }
    }

    private static boolean copycatWheels$renderOne(
            WheelMountBlockEntity mount,
            Pose3dc subLevelPose,
            PoseStack ms,
            MultiBufferSource.BufferSource buffer,
            BlockRenderDispatcher dispatcher,
            ClientLevel level,
            Vec3 camPos,
            float partialTick
    ) {
        if (!(mount instanceof CopycatWheelAccess access)) return false;

        ItemStack held = mount.getHeldItem();
        if (held.isEmpty()) return false;

        ResourceLocation blockId = access.copycatWheels$getCopycatBlock();
        if (blockId == null) return false;

        Block block = BuiltInRegistries.BLOCK.get(blockId);
        BlockState blockState = block.defaultBlockState();
        if (blockState.isAir()) return false;

        if (CopycatRenderState.gunmuMode && blockState.is(BlockTags.LOGS)) return true;

        BlockPos pos = mount.getBlockPos();
        int light = LightTexture.pack(
                level.getBrightness(LightLayer.BLOCK, pos),
                level.getBrightness(LightLayer.SKY, pos)
        );

        BaseTransform baseT = copycatWheels$baseTransforms.get(blockId);

        if (CopycatRenderState.renderBaseChannel) {
            ms.pushPose();
            copycatWheels$applyWorldTransform(ms, pos, subLevelPose, camPos);
            copycatWheels$applyOffset(ms, access);
            copycatWheels$applyWheelMountPose(ms, mount, partialTick);
            copycatWheels$applyUserRotation(ms, access);
            copycatWheels$applyScale(ms, access);

            if (baseT != null) {
                ms.translate(baseT.tx(), baseT.ty(), baseT.tz());
                if (baseT.axis() != null && baseT.degrees() != 0f) {
                    ms.mulPose(baseT.axis().rotationDegrees(baseT.degrees()));
                }
            }

            ms.translate(-0.5, -0.5, -0.5);
            dispatcher.renderSingleBlock(blockState, ms, buffer, light, OverlayTexture.NO_OVERLAY);
            ms.popPose();
        }

        if (CopycatRenderState.renderOverrideChannel) {
            ResourceLocation override = copycatWheels$getOverrideModel(blockId);
            if (override != null) {
                ms.pushPose();
                copycatWheels$applyWorldTransform(ms, pos, subLevelPose, camPos);
                copycatWheels$applyOffset(ms, access);
                copycatWheels$applyWheelMountPose(ms, mount, partialTick);
                copycatWheels$applyUserRotation(ms, access);
                copycatWheels$applyScale(ms, access);

                ms.translate(-0.5, -0.5, -0.5);
                copycatWheels$renderCustomModel(override, blockState, ms, buffer, light);
                ms.popPose();
            }
        }

        return true;
    }

    private static void copycatWheels$applyOffset(final PoseStack ms, final CopycatWheelAccess access) {
        final float x = access.copycatWheels$getOffsetX();
        final float y = access.copycatWheels$getOffsetY();
        final float z = access.copycatWheels$getOffsetZ();
        if (x == 0f && y == 0f && z == 0f) return;
        ms.translate(x, y, z);
    }

    private static void copycatWheels$applyUserRotation(final PoseStack ms, final CopycatWheelAccess access) {
        final float[] q = access.copycatWheels$getCopycatRotation();
        if (q == null) return;
        if (q[0] == 0f && q[1] == 0f && q[2] == 0f && q[3] == 1f) return;
        ms.mulPose(new Quaternionf(q[0], q[1], q[2], q[3]));
    }

    private static void copycatWheels$applyScale(final PoseStack ms, final CopycatWheelAccess access) {
        final float s = access.copycatWheels$getCopycatScale();
        if (s == 1.0f) return;
        ms.scale(s, s, s);
    }

    private static void copycatWheels$applyWorldTransform(
            PoseStack ms, BlockPos pos, Pose3dc subLevelPose, Vec3 camPos
    ) {
        if (subLevelPose != null) {
            Vec3 worldPos = subLevelPose.transformPosition(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
            ms.translate(worldPos.x - camPos.x, worldPos.y - camPos.y, worldPos.z - camPos.z);

            Vec3 ex = subLevelPose.transformNormal(new Vec3(1, 0, 0));
            Vec3 ey = subLevelPose.transformNormal(new Vec3(0, 1, 0));
            Vec3 ez = subLevelPose.transformNormal(new Vec3(0, 0, 1));

            Matrix4f rot = new Matrix4f(
                    (float) ex.x, (float) ex.y, (float) ex.z, 0,
                    (float) ey.x, (float) ey.y, (float) ey.z, 0,
                    (float) ez.x, (float) ez.y, (float) ez.z, 0,
                    0, 0, 0, 1
            );
            ms.last().pose().mul(rot);
        } else {
            ms.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
        }
    }

    private static ResourceLocation copycatWheels$getOverrideModel(ResourceLocation blockId) {
        Boolean cached = copycatWheels$overrideCache.get(blockId);
        if (cached != null) return cached ? copycatWheels$deriveOverridePath(blockId) : null;

        final ResourceLocation direct = copycatWheels$deriveOverridePath(blockId);
        if (copycatWheels$resourceExists(direct)) {
            copycatWheels$overrideCache.put(blockId, true);
            return direct;
        }

        final ResourceLocation alias = copycatWheels$aliases.get(blockId);
        if (alias != null) {
            final ResourceLocation aliasPath = copycatWheels$deriveOverridePath(alias);
            if (copycatWheels$resourceExists(aliasPath)) {
                copycatWheels$overrideCache.put(blockId, true);
                CopycatWheelsMod.LOGGER.info("[CW-OVERRIDE] {} -> alias {}", blockId, alias);
                return aliasPath;
            }
        }

        copycatWheels$overrideCache.put(blockId, false);
        return null;
    }

    private static boolean copycatWheels$resourceExists(final ResourceLocation model) {
        final ResourceLocation jsonPath = ResourceLocation.fromNamespaceAndPath(
                model.getNamespace(), "models/" + model.getPath() + ".json");
        try {
            return Minecraft.getInstance().getResourceManager().getResource(jsonPath).isPresent();
        } catch (Exception e) { return false; }
    }

    private static ResourceLocation copycatWheels$deriveOverridePath(ResourceLocation blockId) {
        return ResourceLocation.fromNamespaceAndPath("offroad", "block/overrides/" + blockId.getPath());
    }

    private static void copycatWheels$renderCustomModel(
            ResourceLocation modelLoc, BlockState blockState,
            PoseStack ms, MultiBufferSource.BufferSource buffer, int light
    ) {
        try {
            PartialModel pm = PartialModel.of(modelLoc);
            SuperByteBuffer buf = CachedBuffers.partial(pm, blockState);
            VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());
            buf.light(light).renderInto(ms, vb);
        } catch (Throwable t) {
            CopycatWheelsMod.LOGGER.warn("[CW-OVERRIDE] failed to render custom model {}", modelLoc, t);
        }
    }

    private static void copycatWheels$applyWheelMountPose(PoseStack ms, WheelMountBlockEntity mount, float partialTick) {
        copycatWheels$applyTracksOffset(ms, mount, partialTick);

        Direction direction = mount.getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();

        float angle = mount.getLerpedAngle(partialTick);
        double extension = mount.getLerpedExtension(partialTick);
        double yaw = copycatWheels$getLerpedYaw(mount, partialTick);

        float yRot = switch (direction) {
            case NORTH -> 180f;
            case EAST  -> 90f;
            case SOUTH -> 0f;
            case WEST  -> -90f;
            default    -> 0f;
        };

        ms.translate(0.5, 0.5, 0.5);
        ms.mulPose(Axis.YP.rotationDegrees(yRot));
        ms.translate(-0.5, -0.5, -0.5);

        final double horizontalWheelPosition = 22.0 / 16.0;
        final double verticalWheelPosition = -extension;

        ms.translate(0.0, verticalWheelPosition, 26.0 / 16.0 - horizontalWheelPosition);

        double pivotZ = -horizontalWheelPosition + 6.0 / 16.0;
        ms.translate(0.5, 0.5, 0.5);
        ms.translate(0.0, 0.0, pivotZ);
        ms.mulPose(Axis.YP.rotation((float) yaw));
        ms.translate(0.0, 0.0, -pivotZ);
        ms.translate(-0.5, -0.5, -0.5);

        ms.translate(0.5, 0.5, 0.5);
        ms.translate(0.0, 0.0, -26.0 / 16.0f);

        final double signMultiplier = -angle
                * (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 : -1.0)
                * (direction.getAxis() == Direction.Axis.X ? 1.0 : -1.0);

        ms.mulPose(Axis.ZP.rotation((float) signMultiplier));

        ItemStack held = mount.getHeldItem();
        TireLike tireLike = held.get(OffroadDataComponents.TIRE);
        if (tireLike != null) {
            Vec3 rotation = tireLike.rotation();
            ms.mulPose(Axis.XP.rotationDegrees((float) rotation.x));
            ms.mulPose(Axis.YP.rotationDegrees((float) rotation.y));
            ms.mulPose(Axis.ZP.rotationDegrees((float) rotation.z));
            ms.translate(tireLike.offset().x, tireLike.offset().y, tireLike.offset().z);
        }
    }

    private static void copycatWheels$applyTracksOffset(PoseStack ms, WheelMountBlockEntity mount, float partialTick) {
        if (!copycatWheels$tracksChecked) {
            copycatWheels$tracksChecked = true;
            try {
                final Class<?> offsetAccess = Class.forName(
                        "dev.qwxon.tracks.mixin_interface.WheelMountOffsetAccess");
                copycatWheels$tracksLateralMethod = offsetAccess.getMethod(
                        "tracks$getLerpedLateralOffset", float.class);
                copycatWheels$tracksLongitudinalMethod = offsetAccess.getMethod(
                        "tracks$getLerpedLongitudinalOffset", float.class);
                copycatWheels$tracksHeightMethod = offsetAccess.getMethod(
                        "tracks$getLerpedHeightOffset", float.class);

                copycatWheels$tracksLateralMethod.setAccessible(true);
                copycatWheels$tracksLongitudinalMethod.setAccessible(true);
                copycatWheels$tracksHeightMethod.setAccessible(true);
            } catch (final Throwable t) {
                copycatWheels$tracksLateralMethod = null;
                copycatWheels$tracksLongitudinalMethod = null;
                copycatWheels$tracksHeightMethod = null;
            }
        }

        if (copycatWheels$tracksLateralMethod == null) return;

        try {
            final double lateral = (double) copycatWheels$tracksLateralMethod.invoke(mount, partialTick);
            final double longitudinal = (double) copycatWheels$tracksLongitudinalMethod.invoke(mount, partialTick);
            final double height = (double) copycatWheels$tracksHeightMethod.invoke(mount, partialTick);

            if (lateral == 0.0 && longitudinal == 0.0 && height == 0.0) return;

            final Direction facing = mount.getBlockState()
                    .getValue(BlockStateProperties.HORIZONTAL_FACING);
            final Direction lateralDir = facing.getClockWise();

            final double dx = lateralDir.getStepX() * lateral - facing.getStepX() * longitudinal;
            final double dz = lateralDir.getStepZ() * lateral - facing.getStepZ() * longitudinal;

            ms.translate(dx, height, dz);
        } catch (Throwable t) {
            //CopycatWheelsMod.LOGGER.warn("[CW-TRACKS] offset invocation failed, disabling", t);
            copycatWheels$tracksLateralMethod = null;
        }
    }

    private static double copycatWheels$getLerpedYaw(WheelMountBlockEntity mount, float partialTick) {
        if (copycatWheels$lerpedYawFailed) return 0.0;
        if (copycatWheels$lerpedYawMethod == null) {
            try {
                copycatWheels$lerpedYawMethod = WheelMountBlockEntity.class
                        .getDeclaredMethod("getLerpedYaw", double.class);
                copycatWheels$lerpedYawMethod.setAccessible(true);
            } catch (Exception e) {
                //CopycatWheelsMod.LOGGER.warn("[CopycatWheels] getLerpedYaw reflection failed", e);
                copycatWheels$lerpedYawFailed = true;
                return 0.0;
            }
        }
        try {
            return (double) copycatWheels$lerpedYawMethod.invoke(mount, (double) partialTick);
        } catch (Exception e) { return 0.0; }
    }
}