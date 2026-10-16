package com.suting.copycatwheels.client;

import com.suting.copycatwheels.CopycatWheelsBlockEntityTypes;
import com.suting.copycatwheels.CopycatWheelsMod;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = CopycatWheelsMod.MODID, value = Dist.CLIENT)
public class CopycatWheelsClientRendererRegistration {

    @SubscribeEvent
    public static void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                CopycatWheelsBlockEntityTypes.COPYCAT_WHEEL_MOUNT.get(),
                copycatWheels$createProvider()
        );
    }

    @SuppressWarnings("unchecked")
    private static BlockEntityRendererProvider<WheelMountBlockEntity> copycatWheels$createProvider() {
        try {
            final Class<?> cls = Class.forName(
                    "dev.qwxon.tracks.content.blocks.wheel_mount.AdjustableWheelMountRenderer");
            //CopycatWheelsMod.LOGGER.info(
             //       "[CW-RENDER] Create: Tracks detected, using AdjustableWheelMountRenderer for copycat mount");
            return ctx -> {
                try {
                    return (BlockEntityRenderer<WheelMountBlockEntity>) cls
                            .getConstructor(BlockEntityRendererProvider.Context.class)
                            .newInstance(ctx);
                } catch (final Throwable t) {
                    CopycatWheelsMod.LOGGER.warn(
                            "[CW-RENDER] failed to instantiate AdjustableWheelMountRenderer, fallback", t);
                    return new WheelMountRenderer(ctx);
                }
            };
        } catch (final ClassNotFoundException e) {
            CopycatWheelsMod.LOGGER.info(
                    "[CW-RENDER] Create: Tracks not present, using WheelMountRenderer for copycat mount");
            return WheelMountRenderer::new;
        }
    }
}