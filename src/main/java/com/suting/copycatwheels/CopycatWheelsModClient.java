package com.suting.copycatwheels;

import com.suting.copycatwheels.client.CopycatWheelsClientColors;
import com.suting.copycatwheels.client.CopycatWheelsModelRegistration;
import com.suting.copycatwheels.core.CopycatConfig;
import com.suting.copycatwheels.core.CopycatRenderState;
import com.suting.copycatwheels.registry.CopycatWheelsBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CopycatWheelsMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CopycatWheelsMod.MODID, value = Dist.CLIENT)
public class CopycatWheelsModClient {
    public CopycatWheelsModClient(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        CopycatWheelsModelRegistration.register(modEventBus);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        CopycatRenderState.camoHintEnabled = CopycatConfig.SHOW_CAMO_HINT.get();

        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(
                    CopycatWheelsBlocks.COPYCAT_WHEEL_MOUNT.get(),
                    RenderType.translucent()
            );
            CopycatWheelsClientColors.register();
        });
    }
}