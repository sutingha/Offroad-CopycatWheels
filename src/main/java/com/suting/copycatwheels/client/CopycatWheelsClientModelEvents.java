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

        final ModelResourceLocation skinKeyI = ModelResourceLocation.standalone(CopycatWheelsModelRegistration.SKIN_MODEL);
        final BakedModel skinModelI = models.get(skinKeyI);
        if (skinModelI == null) {
            CopycatWheelsMod.LOGGER.warn("[CW-MODEL] skin model I not found: {}", skinKeyI);
        }

        final ModelResourceLocation skinKeyII = ModelResourceLocation.standalone(CopycatWheelsModelRegistration.SKIN_MODEL_II);
        final BakedModel skinModelII = models.get(skinKeyII);
        if (skinModelII == null) {
            CopycatWheelsMod.LOGGER.warn("[CW-MODEL] skin model II not found: {}", skinKeyII);
        }

        final List<ModelResourceLocation> targets = new ArrayList<>();
        for (final ModelResourceLocation key : models.keySet()) {
            if (key.id().getNamespace().equals(CopycatWheelsMod.MODID)) {
                String path = key.id().getPath();
                if (path.equals("copycat_wheel_mount") || path.equals("copycat_wheel_mount_ii")) {
                    targets.add(key);
                }
            }
        }

        for (final ModelResourceLocation key : targets) {
            final BakedModel original = models.get(key);
            if (original == null) continue;
            
            if (key.id().getPath().equals("copycat_wheel_mount")) {
                models.put(key, new CopycatWheelMountModel(original, skinModelI));
                CopycatWheelsMod.LOGGER.info("[CW-MODEL] wrapped I {} (skin={})", key, skinModelI != null);
            } else if (key.id().getPath().equals("copycat_wheel_mount_ii")) {
                models.put(key, new CopycatWheelMountModel(original, skinModelII));
                CopycatWheelsMod.LOGGER.info("[CW-MODEL] wrapped II {} (skin={})", key, skinModelII != null);
            }
        }
    }
}