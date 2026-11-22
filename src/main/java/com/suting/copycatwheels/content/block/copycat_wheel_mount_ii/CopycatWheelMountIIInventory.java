package com.suting.copycatwheels.content.block.copycat_wheel_mount_ii;

import dev.simulated_team.simulated.multiloader.inventory.MultiSlotContainer;
import net.minecraft.world.item.ItemStack;

public class CopycatWheelMountIIInventory extends MultiSlotContainer {
    private final CopycatWheelMountIIBlockEntity blockEntity;
    public boolean suppressUpdate;

    public CopycatWheelMountIIInventory(CopycatWheelMountIIBlockEntity blockEntity) {
        super(2);
        this.blockEntity = blockEntity;
        this.suppressUpdate = false;
    }

    @Override
    public void setChanged() {
        if (!this.suppressUpdate) {
            this.blockEntity.onStackChanged();
            this.blockEntity.notifyUpdate();
        }
    }

    public ItemStack getLeftWheel() { return this.getItem(0); }
    public ItemStack getRightWheel() { return this.getItem(1); }
    public void setLeftWheel(ItemStack stack) { this.setItem(0, stack); }
    public void setRightWheel(ItemStack stack) { this.setItem(1, stack); }
}