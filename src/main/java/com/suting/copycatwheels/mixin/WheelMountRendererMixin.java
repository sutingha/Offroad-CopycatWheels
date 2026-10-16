package com.suting.copycatwheels.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.suting.copycatwheels.CopycatWheelAccess;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountRenderer;
import dev.ryanhcode.offroad.index.OffroadPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WheelMountRenderer.class)
public class WheelMountRendererMixin {

    @Redirect(
            method = "renderSafe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;"
                            + "renderStatic(Lnet/minecraft/world/item/ItemStack;"
                            + "Lnet/minecraft/world/item/ItemDisplayContext;"
                            + "IILcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/MultiBufferSource;"
                            + "Lnet/minecraft/world/level/Level;I)V"
            )
    )
    private void copycatWheels$redirectRenderStatic(
            final ItemRenderer itemRenderer,
            final ItemStack itemStack,
            final ItemDisplayContext displayContext,
            final int light,
            final int overlay,
            final PoseStack ms,
            final MultiBufferSource buffer,
            final Level level,
            final int seed,
            @Local(argsOnly = true) final WheelMountBlockEntity be
    ) {
        if (be instanceof final CopycatWheelAccess access
                && access.copycatWheels$getCopycatBlock() != null) {
            return;
        }
        itemRenderer.renderStatic(itemStack, displayContext, light, overlay, ms, buffer, level, seed);
    }

    @Redirect(
            method = "renderSafe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/render/CachedBuffers;partial("
                            + "Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"
                            + "Lnet/minecraft/world/level/block/state/BlockState;"
                            + ")Lnet/createmod/catnip/render/SuperByteBuffer;"
            )
    )
    private SuperByteBuffer copycatWheels$redirectPartial(
            final PartialModel model,
            final BlockState state,
            @Local(argsOnly = true) final WheelMountBlockEntity be
    ) {
        if (be instanceof final CopycatWheelAccess access
                && access.copycatWheels$getCopycatBlock() != null
                && !copycatWheels$isStructuralModel(model)) {
            return CachedBuffers.partial(model, state).color(0, 0, 0, 0);
        }
        return CachedBuffers.partial(model, state);
    }

    private static boolean copycatWheels$isStructuralModel(final PartialModel model) {
        return model == OffroadPartialModels.DIODE_LEFT
                || model == OffroadPartialModels.DIODE_RIGHT
                || model == OffroadPartialModels.TELE_OUTER
                || model == OffroadPartialModels.TELE_INNER
                || model == OffroadPartialModels.TELE_MOUNT
                || model == OffroadPartialModels.SPRING_UPPER
                || model == OffroadPartialModels.SPRING_LOWER
                || model == OffroadPartialModels.SPRING_MIDDLE;
    }
}