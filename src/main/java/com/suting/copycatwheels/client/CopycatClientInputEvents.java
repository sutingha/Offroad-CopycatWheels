package com.suting.copycatwheels.client;

import com.suting.copycatwheels.CopycatItems;
import com.suting.copycatwheels.CopycatRenderState;
import com.suting.copycatwheels.CopycatWheelsMod;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = CopycatWheelsMod.MODID, value = Dist.CLIENT)
public class CopycatClientInputEvents {

    private static final int MODE_COUNT = 7;

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
        final Minecraft mc = Minecraft.getInstance();
        final Player player = mc.player;
        if (player == null) return;

        //确定伪装提示是否开启，只是为了overrides渲染做的
        while (CopycatKeys.CAMO_HINT_TOGGLE.consumeClick()) {
            CopycatRenderState.camoHintEnabled = !CopycatRenderState.camoHintEnabled;
            //CopycatWheelsMod.LOGGER.info("[CW-KEY] camo hint toggled: {}",
            //       CopycatRenderState.camoHintEnabled);

            player.displayClientMessage(
                    Component.translatable(CopycatRenderState.camoHintEnabled
                            ? "copycatwheels.msg.camo_hint_on"
                            : "copycatwheels.msg.camo_hint_off"),
                    false);
        }

        //Alt滚轮模式提示,
        if (!Screen.hasAltDown()) return;
        if (!copycatWheels$holdingDebugger(player)) return;

        final String modeKey = switch (CopycatRenderState.currentToolMode) {
            case 0 -> "copycatwheels.mode.remove";
            case 1 -> "copycatwheels.mode.rotate";
            case 2 -> "copycatwheels.mode.scale";
            case 3 -> "copycatwheels.mode.collision";
            case 4 -> "copycatwheels.mode.friction";
            case 5 -> "copycatwheels.mode.position";
            default -> "copycatwheels.mode.reset";
        };

        mc.gui.setOverlayMessage(
                Component.translatable(modeKey)
                        .copy()
                        .append(Component.literal(" §8[§7滚轮切换§8]")),
                false);
    }

    @SubscribeEvent
    public static void onMouseScroll(final InputEvent.MouseScrollingEvent event) {
        if (!Screen.hasAltDown()) return;
        final Minecraft mc = Minecraft.getInstance();
        final Player player = mc.player;
        if (player == null) return;
        if (!copycatWheels$holdingDebugger(player)) return;

        final double delta = event.getScrollDeltaY();
        if (delta == 0) return;

        int next = CopycatRenderState.currentToolMode;
        if (delta > 0) next = (next + 1) % MODE_COUNT;
        else next = (next - 1 + MODE_COUNT) % MODE_COUNT;
        CopycatRenderState.currentToolMode = next;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        final Player player = event.getEntity();
        if (player == null) return;

        final ItemStack stack = event.getItemStack();
        if (!copycatWheels$isDebugger(stack)) return;

        final BlockPos pos = event.getPos();
        final BlockEntity be = player.level().getBlockEntity(pos);
        if (!(be instanceof WheelMountBlockEntity)) return;
        if (!player.level().isClientSide) return;

        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);

        final Direction hitFace = event.getFace();
        if (hitFace == null) return;

        final int m = CopycatRenderState.currentToolMode;
        final boolean sneaking = player.isShiftKeyDown();

        float delta;
        switch (m) {
            case 0, 6 -> delta = 0f;
            case 1 -> delta = sneaking ? -90f : 90f;
            case 2 -> delta = sneaking ? -0.1f : 0.1f;
            case 3 -> delta = sneaking ? -0.05f : 0.05f;
            case 4 -> delta = sneaking ? -0.1f : 0.1f;
            default -> delta = sneaking ? -0.05f : 0.05f;
        }

        final Minecraft mc = Minecraft.getInstance();
        final ClientPacketListener conn = mc.getConnection();
        if (conn != null) {
            final String cmd = "copycatwheels debug "
                    + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                    + " " + m + " " + hitFace.getName() + " " + delta;
            conn.sendCommand(cmd);
        }
    }

    private static boolean copycatWheels$holdingDebugger(final Player player) {
        return copycatWheels$isDebugger(player.getMainHandItem())
                || copycatWheels$isDebugger(player.getOffhandItem());
    }

    private static boolean copycatWheels$isDebugger(final ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (CopycatItems.COPYCAT_REMOVER == null) return false;
        return stack.is(CopycatItems.COPYCAT_REMOVER.get());
    }
}