package com.suting.copycatwheels.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.suting.copycatwheels.content.block.copycat_wheel_mount_ii.CopycatWheelMountIIBlock;
import com.suting.copycatwheels.content.block.copycat_wheel_mount_ii.CopycatWheelMountIIBlockEntity;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import dev.ryanhcode.offroad.index.OffroadPartialModels;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector2d;

public class CopycatWheelMountIIRenderer extends KineticBlockEntityRenderer<CopycatWheelMountIIBlockEntity> {
    public CopycatWheelMountIIRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(CopycatWheelMountIIBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = getRenderedBlockState(be);
        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(CopycatWheelMountIIBlock.HORIZONTAL_FACING);

        SuperByteBuffer shaftFront = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, facing);
        renderRotatingBuffer(be, shaftFront, ms, buffer.getBuffer(getRenderType(be, state)), light);

        SuperByteBuffer shaftBack = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, facing.getOpposite());
        renderRotatingBuffer(be, shaftBack, ms, buffer.getBuffer(getRenderType(be, state)), light);

        FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);

        renderWheelAssembly(be, be.getLeftWheel(),  be.getLerpedLeftAngle(partialTicks),  be.getLerpedLeftExtension(partialTicks),
                facing.getClockWise(), be.getLerpedSteeringYaw(partialTicks), true,
                blockState, ms, buffer, light, overlay, be.getLerpedRenderOffset(partialTicks, true));
        renderWheelAssembly(be, be.getRightWheel(), be.getLerpedRightAngle(partialTicks), be.getLerpedRightExtension(partialTicks),
                facing.getCounterClockWise(), be.getLerpedSteeringYaw(partialTicks), false,
                blockState, ms, buffer, light, overlay, be.getLerpedRenderOffset(partialTicks, false));
    }

    public static void renderWheelAssembly(CopycatWheelMountIIBlockEntity be, ItemStack stack, float wheelAngle, double extension, Direction wheelDirection, double steeringYaw, boolean leftWheel, BlockState state, PoseStack ms, MultiBufferSource buffer, int light, int overlay, Vec3 offset) {
        TireLike tire = stack.get(OffroadDataComponents.TIRE);
        double offX = offset.x * wheelDirection.getStepZ() - offset.z * wheelDirection.getStepX();
        double offZ = offset.x * wheelDirection.getStepX() + offset.z * wheelDirection.getStepZ();
        double verticalWheelPosition = -extension + offset.y;
        double teleAngle = Math.atan2(verticalWheelPosition - -0.375, 0.75);
        double teleDistance = new Vector2d(verticalWheelPosition - -0.375, 0.75).length();
        double springAngle = Math.atan2(verticalWheelPosition - -0.125 - 0.4375, 0.1875);
        double springDistance = new Vector2d(verticalWheelPosition - -0.125 - 0.4375, 0.1875).length();

        VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        SuperByteBuffer teleOuter = CachedBuffers.partial(OffroadPartialModels.TELE_OUTER, state);
        SuperByteBuffer teleInner = CachedBuffers.partial(OffroadPartialModels.TELE_INNER, state);
        SuperByteBuffer teleMount = CachedBuffers.partial(OffroadPartialModels.TELE_MOUNT, state);
        SuperByteBuffer springTop = CachedBuffers.partial(OffroadPartialModels.SPRING_UPPER, state);
        SuperByteBuffer springMiddle = CachedBuffers.partial(OffroadPartialModels.SPRING_MIDDLE, state);
        SuperByteBuffer springBottom = CachedBuffers.partial(OffroadPartialModels.SPRING_LOWER, state);

        ResourceLocation sideCopycatBlock = leftWheel ? be.getLeftCopycatBlock() : be.getRightCopycatBlock();
        float[] sideRotation = leftWheel ? be.getLeftRotation() : be.getRightRotation();
        boolean isCopycatThisSide = be.isCopycatWheel(stack) && sideCopycatBlock != null;

        ms.pushPose();

        TransformStack.of(ms).center().rotateYDegrees(AngleHelper.horizontalAngle(wheelDirection)).rotateXDegrees(AngleHelper.verticalAngle(wheelDirection)).uncenter();

        if (!be.copycatWheels$isVisualSuspensionHidden()) {
            ms.pushPose();
            ms.translate(offX, -0.375, offZ);
            ms.translate(0.5, 0.5, 0.5);
            ms.mulPose(Axis.XP.rotation((float)teleAngle));
            ms.translate(-0.5, -0.5, -0.5);
            teleOuter.light(light).renderInto(ms, vb);
            ms.translate(0.0, 0.0, -(teleDistance - 1.0));
            teleInner.light(light).renderInto(ms, vb);
            ms.popPose();

            ms.pushPose();
            ms.translate(0.5 + offX, 0.9375, 0.5 + offZ - 0.4375);
            ms.mulPose(Axis.XP.rotation((float)springAngle + (float) (Math.PI / 2)));
            ms.translate(-0.5 - offX, -0.9375, -0.5 - offZ + 0.4375);
            float springSpan = (float)springDistance - 0.25F;
            springTop.light(light).renderInto(ms, vb);
            springMiddle.light(light).translate(0.0F, 0.8125F, 0.0F).scale(1.0F, springSpan / 0.875F, 1.0F).translateBack(0.0F, 0.8125F, 0.0F).renderInto(ms, vb);
            springBottom.light(light).translate(0.0, -(springSpan - 0.875), 0.0).renderInto(ms, vb);
            ms.popPose();
        }

        ms.pushPose();
        ms.translate(offX, verticalWheelPosition, offZ + 1.625 - 1.375);
        ms.translate(0.5, 0.5, 0.5);
        ms.rotateAround(Axis.YP.rotation((float)steeringYaw), 0.0F, 0.0F, -1.0F);
        ms.translate(-0.5, -0.5, -0.5);

        if (!be.copycatWheels$isVisualSuspensionHidden()) {
            teleMount.light(light).renderInto(ms, vb);
        }

        ms.translate(0.5, 0.5, 0.5);
        ms.translate(0.0, 0.0, -1.625);

        double spinSign = (wheelDirection.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 : -1.0)
                * (wheelDirection.getAxis() == Direction.Axis.X ? 1.0 : -1.0);
        Direction mountFacing = state.getValue(CopycatWheelMountIIBlock.HORIZONTAL_FACING);
        if (mountFacing == Direction.NORTH || mountFacing == Direction.WEST) spinSign *= -1.0;

        if (isCopycatThisSide) {
            ms.pushPose();

            float ox = be.copycatWheels$getOffsetX();
            float oy = be.copycatWheels$getOffsetY();
            float oz = be.copycatWheels$getOffsetZ();

            //镜像（右轮
            if (!leftWheel) {
                float nx = wheelDirection.getStepX();
                float nz = wheelDirection.getStepZ();
                float dot = ox * nx + oz * nz;
                ox -= 2 * dot * nx;
                oz -= 2 * dot * nz;
            }

            if (ox != 0f || oy != 0f || oz != 0f) {
                float totalAngle = (float) Math.toRadians(AngleHelper.horizontalAngle(wheelDirection)) + (float) steeringYaw;
                float cosT = (float) Math.cos(totalAngle);
                float sinT = (float) Math.sin(totalAngle);
                float localOx = ox * cosT - oz * sinT;
                float localOz = ox * sinT + oz * cosT;
                ms.translate(localOx, oy, localOz);
            }

            if (sideRotation != null) {
                ms.mulPose(new Quaternionf(sideRotation[0], sideRotation[1], sideRotation[2], sideRotation[3]));
            }
            ms.mulPose(Axis.ZP.rotation((float)(-wheelAngle * spinSign)));

            float s = be.getCopycatScale();
            if (s != 1.0f) ms.scale(s, s, s);

            ms.scale(2.0f, 2.0f, 2.0f);

            BlockState blockState = BuiltInRegistries.BLOCK.get(sideCopycatBlock).defaultBlockState();
            ItemStack blockItemStack = new ItemStack(blockState.getBlock());

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    blockItemStack,
                    ItemDisplayContext.FIXED,
                    light,
                    overlay,
                    ms,
                    buffer,
                    be.getLevel(),
                    0
            );
            ms.popPose();
        } else if (tire != null) {
            ms.mulPose(Axis.ZP.rotation((float)(-wheelAngle * spinSign)));

            Vec3 rotation = tire.rotation();
            ms.mulPose(Axis.XP.rotation((float)Math.toRadians(rotation.x)));
            ms.mulPose(Axis.YP.rotation((float)Math.toRadians(rotation.y)));
            ms.mulPose(Axis.ZP.rotation((float)Math.toRadians(rotation.z)));
            ms.translate(tire.offset().x, tire.offset().y, tire.offset().z);
            if (tire.model().isPresent()) {
                CachedBuffers.partial(PartialModel.of(tire.model().get()), state).light(light).translate(-0.5F, 0.0F, -0.5F).renderInto(ms, vb);
            } else {
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light, overlay, ms, buffer, be.getLevel(), 0);
            }
        }
        ms.popPose();
        ms.popPose();
    }

    @Override
    public int getViewDistance() { return 512; }

    @Override
    protected SuperByteBuffer getRotatedModel(CopycatWheelMountIIBlockEntity te, BlockState state) {
        Direction facing = state.getValue(CopycatWheelMountIIBlock.HORIZONTAL_FACING);
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, facing);
    }
}