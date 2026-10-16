package com.suting.copycatwheels.client.model;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public final class CopycatWheelModelProperties {
    public static final ModelProperty<BlockState> MATERIAL_PROPERTY = new ModelProperty<>();
    public static final ModelProperty<Boolean> EMISSIVE_PROPERTY = new ModelProperty<>();

    private CopycatWheelModelProperties() {}
}