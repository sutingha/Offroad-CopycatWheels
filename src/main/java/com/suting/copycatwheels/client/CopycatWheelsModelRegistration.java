package com.suting.copycatwheels.client;

import com.suting.copycatwheels.CopycatWheelsMod;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional;

public final class CopycatWheelsModelRegistration {

    public static final ResourceLocation SKELETON_MODEL = ResourceLocation.fromNamespaceAndPath(
            "offroad", "block/copycat_wheel_mount/copycat_wheel_mount_skeleton");

    public static final ResourceLocation SKIN_MODEL = ResourceLocation.fromNamespaceAndPath(
            "offroad", "block/copycat_wheel_mount/copycat_wheel_mount_skin");

    public static final ResourceLocation SKELETON_MODEL_II = ResourceLocation.fromNamespaceAndPath(
            "offroad", "block/copycat_wheel_mount_ii/copycat_wheel_mount_ii_skeleton");

    public static final ResourceLocation SKIN_MODEL_II = ResourceLocation.fromNamespaceAndPath(
            "offroad", "block/copycat_wheel_mount_ii/copycat_wheel_mount_ii_skin");

    private CopycatWheelsModelRegistration() {}

    public static void register(final IEventBus modEventBus) {
        modEventBus.addListener(CopycatWheelsModelRegistration::onRegisterAdditional);
    }

    private static void onRegisterAdditional(final RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(SKELETON_MODEL));
        event.register(ModelResourceLocation.standalone(SKIN_MODEL));
        event.register(ModelResourceLocation.standalone(SKELETON_MODEL_II));
        event.register(ModelResourceLocation.standalone(SKIN_MODEL_II));

        CopycatWheelsMod.LOGGER.info("[CW-REG] registered skeleton/skin models (I & II)");
    }
}