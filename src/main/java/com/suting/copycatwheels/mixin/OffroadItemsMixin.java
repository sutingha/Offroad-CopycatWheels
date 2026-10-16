package com.suting.copycatwheels.mixin;

import com.suting.copycatwheels.CopycatItems;
import com.suting.copycatwheels.CopycatWheelsBlocks;
import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.content.items.tire.TireItem;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import dev.ryanhcode.offroad.index.OffroadItems;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(OffroadItems.class)
public class OffroadItemsMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addTire(final CallbackInfo ci) {
        final SimulatedRegistrate REGISTRATE = Offroad.getRegistrate();

        final ResourceLocation originalSection = copycatWheels$getCurrentSection(REGISTRATE);
        final ResourceLocation copycatSection =
                ResourceLocation.fromNamespaceAndPath("copycatwheels", "copycat_wheels");

        try {
            REGISTRATE.inSection(copycatSection);

            CopycatItems.COPYCAT_WHEEL_MOUNT = REGISTRATE
                    .item("copycat_wheel_mount",
                            (props) -> new BlockItem(CopycatWheelsBlocks.COPYCAT_WHEEL_MOUNT.get(), props))
                    .model((ctx, prov) -> prov.withExistingParent(
                            ctx.getName(),
                            ResourceLocation.fromNamespaceAndPath("copycatwheels", "block/copycat_wheel_mount_empty")))
                    .register();

            CopycatItems.COPYCAT_WHEEL =
                    REGISTRATE.item("copycat_wheel", TireItem::new)
                            .properties((x) -> x.component(OffroadDataComponents.TIRE, TireLike.TIRE))
                            .defaultModel()
                            .register();

            // 材质伪装轮已废弃
            // final ResourceLocation tireModel = ResourceLocation.fromNamespaceAndPath(
            //         "copycatwheels", "block/copycat_tire");
            // final TireLike copycatTireSpec = new TireLike(
            //         TireLike.TIRE.radius(),
            //         new Vec3(90, 0, 0),
            //         Vec3.ZERO,
            //         tireModel);
            // CopycatItems.COPYCAT_TIRE =
            //         REGISTRATE.item("copycat_tire", TireItem::new)
            //                 .properties((x) -> x.component(OffroadDataComponents.TIRE, copycatTireSpec))
            //                 .defaultModel()
            //                 .register();

            CopycatItems.COPYCAT_REMOVER =
                    REGISTRATE.item("copycat_remover", Item::new)
                            .properties((x) -> x.stacksTo(1))
                            .defaultModel()
                            .register();
        } finally {
            if (originalSection != null) {
                REGISTRATE.inSection(originalSection);
            }
        }
    }

    private static ResourceLocation copycatWheels$getCurrentSection(final SimulatedRegistrate registrate) {
        try {
            final Field f = SimulatedRegistrate.class.getDeclaredField("currentSection");
            f.setAccessible(true);
            return (ResourceLocation) f.get(registrate);
        } catch (final Throwable t) {
            return null;
        }
    }
}