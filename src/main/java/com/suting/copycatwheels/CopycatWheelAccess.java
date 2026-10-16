package com.suting.copycatwheels;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface CopycatWheelAccess {
    void copycatWheels$setCopycatBlock(@Nullable ResourceLocation id);
    @Nullable ResourceLocation copycatWheels$getCopycatBlock();

    void copycatWheels$setCopycatRotation(@Nullable float[] xyzw);
    @Nullable float[] copycatWheels$getCopycatRotation();

    void copycatWheels$setCopycatScale(float scale);
    float copycatWheels$getCopycatScale();

    void copycatWheels$setFrictionMultiplier(float m);
    float copycatWheels$getFrictionMultiplier();

    void copycatWheels$setCopycatOffset(float x, float y, float z);
    float copycatWheels$getOffsetX();
    float copycatWheels$getOffsetY();
    float copycatWheels$getOffsetZ();

    void copycatWheels$setMaterial(@Nullable BlockState state);
    @Nullable BlockState copycatWheels$getMaterial();

    //材质伪装轮,已废弃(
    //void copycatWheels$setTireMaterial(@Nullable BlockState state);
    //@Nullable BlockState copycatWheels$getTireMaterial();
}