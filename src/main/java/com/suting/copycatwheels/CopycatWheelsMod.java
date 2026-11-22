package com.suting.copycatwheels;

import com.mojang.logging.LogUtils;
import com.suting.copycatwheels.core.CopycatConfig;
import com.suting.copycatwheels.event.CopycatWheelsCommands;
import com.suting.copycatwheels.registry.CopycatWheelsBlockEntityTypes;
import com.suting.copycatwheels.registry.CopycatWheelsBlocks;
import dev.ryanhcode.sable.platform.SableEventPlatform;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(CopycatWheelsMod.MODID)
public class CopycatWheelsMod {
    public static final String MODID = "copycatwheels";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredItem<Item> GUNMU_ICON = ITEMS.register(
            "gunmu_icon", () -> new Item(new Item.Properties())
    );

    public CopycatWheelsMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);

        ITEMS.register(modEventBus);

        CopycatWheelsBlocks.register(modEventBus);
        CopycatWheelsBlockEntityTypes.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, CopycatConfig.SPEC);
        
        SableEventPlatform.INSTANCE.onPhysicsTick((system, timeStep) -> {
            com.suting.copycatwheels.content.block.copycat_wheel_mount_ii.CopycatWheelMountIIBlockEntity.applyAllBatchedForces(system, timeStep);
        });

        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    void registerCommands(RegisterCommandsEvent event) {
        CopycatWheelsCommands.register(event.getDispatcher());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Copycat Wheels: common setup");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Copycat Wheels: server starting");
    }
}