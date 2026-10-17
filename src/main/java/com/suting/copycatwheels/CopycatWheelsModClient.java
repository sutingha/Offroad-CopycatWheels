package com.suting.copycatwheels;

import com.suting.copycatwheels.client.CopycatKeys;
import com.suting.copycatwheels.client.CopycatWheelsClientColors;
import com.suting.copycatwheels.client.CopycatWheelsModelRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CopycatWheelsMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CopycatWheelsMod.MODID, value = Dist.CLIENT)
public class CopycatWheelsModClient {
    public CopycatWheelsModClient(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        CopycatWheelsModelRegistration.register(modEventBus);

        modEventBus.addListener((RegisterKeyMappingsEvent event) -> {
            event.register(CopycatKeys.CAMO_HINT_TOGGLE);
        });
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        CopycatWheelsMod.LOGGER.info("Copycat Wheels: client setup");

        CopycatRenderState.camoHintEnabled = CopycatConfig.SHOW_CAMO_HINT.get();

        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(
                    CopycatWheelsBlocks.COPYCAT_WHEEL_MOUNT.get(),
                    RenderType.translucent()  // ★ 改回 translucent
            );
            CopycatWheelsClientColors.register();
        });
    }
}