package com.suting.copycatwheels.client;

import com.suting.copycatwheels.CopycatWheelsMod;
import com.suting.copycatwheels.client.model.CopycatWheelMountModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = CopycatWheelsMod.MODID, value = Dist.CLIENT)
public class CopycatWheelsClientModelEvents {

    @SubscribeEvent
    public static void onModifyBakingResult(final ModifyBakingResult event) {
        final Map<ModelResourceLocation, BakedModel> models = event.getModels();

        final ModelResourceLocation skinKey = ModelResourceLocation.standalone(CopycatWheelsModelRegistration.SKIN_MODEL);
        final BakedModel skinModel = models.get(skinKey);

        if (skinModel == null) {
            CopycatWheelsMod.LOGGER.warn("[CW-MODEL] skin model not found: {}", skinKey);
        }

        final List<ModelResourceLocation> targets = new ArrayList<>();
        for (final ModelResourceLocation key : models.keySet()) {
            if (key.id().getNamespace().equals(CopycatWheelsMod.MODID)
                    && key.id().getPath().equals("copycat_wheel_mount")) {
                targets.add(key);
            }
        }

        for (final ModelResourceLocation key : targets) {
            final BakedModel original = models.get(key);
            if (original == null) continue;
            models.put(key, new CopycatWheelMountModel(original, skinModel));
            CopycatWheelsMod.LOGGER.info("[CW-MODEL] wrapped {} (skin={})", key, skinModel != null);
        }
    }
}