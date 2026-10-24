package com.suting.copycatwheels.client;

import com.suting.copycatwheels.CopycatWheelsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional;

import java.util.Map;

public final class CopycatWheelsModelRegistration {

    private static final String OVERRIDE_DIR = "models/block/overrides";

    public static final ResourceLocation SKELETON_MODEL = ResourceLocation.fromNamespaceAndPath(
            "offroad", "block/copycat_wheel_mount/copycat_wheel_mount_skeleton");

    public static final ResourceLocation SKIN_MODEL = ResourceLocation.fromNamespaceAndPath(
            "offroad", "block/copycat_wheel_mount/copycat_wheel_mount_skin");

    private CopycatWheelsModelRegistration() {}

    public static void register(final IEventBus modEventBus) {
        modEventBus.addListener(CopycatWheelsModelRegistration::onRegisterAdditional);
    }

    private static void onRegisterAdditional(final RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(SKELETON_MODEL));
        event.register(ModelResourceLocation.standalone(SKIN_MODEL));

        final Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        final Map<ResourceLocation, ?> resources;
        try {
            resources = mc.getResourceManager().listResources(
                    OVERRIDE_DIR,
                    loc -> loc.getNamespace().equals("offroad") && loc.getPath().endsWith(".json")
            );
        } catch (final Throwable t) {
            CopycatWheelsMod.LOGGER.warn("[CW-REG] failed to scan override models", t);
            return;
        }

        int count = 0;
        for (final ResourceLocation rl : resources.keySet()) {
            final String path = rl.getPath();
            if (!path.startsWith("models/") || !path.endsWith(".json")) continue;

            final String modelPath = path.substring("models/".length(), path.length() - ".json".length());
            final ResourceLocation modelLoc = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), modelPath);
            event.register(ModelResourceLocation.standalone(modelLoc));
            count++;
        }

        //CopycatWheelsMod.LOGGER.info("[CW-REG] registered {} override models + skeleton/skin", count);
    }
}