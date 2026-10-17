package com.suting.copycatwheels.mixin;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.suting.copycatwheels.CopycatWheelAccess;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Objects;

@Mixin(WheelMountBlockEntity.class)
public abstract class WheelMountBlockEntityMixin implements CopycatWheelAccess {

    private static final float OFFSET_LIMIT = 1.0f;
    private static final float OFFSET_STEP = 0.05f;
    private static final float EPS = 1e-4f;

    @Unique @Nullable private ResourceLocation copycatWheels$copycatBlock = null;
    @Unique @Nullable private float[] copycatWheels$rotation = null;
    @Unique private float copycatWheels$scale = 1.0f;
    @Unique private float copycatWheels$frictionMultiplier = 1.0f;
    @Unique private float copycatWheels$offsetX = 0f;
    @Unique private float copycatWheels$offsetY = 0f;
    @Unique private float copycatWheels$offsetZ = 0f;
    @Unique @Nullable private BlockState copycatWheels$material = null;

    @Override
    public void copycatWheels$setCopycatBlock(@Nullable final ResourceLocation id) {
        if (Objects.equals(this.copycatWheels$copycatBlock, id)) return;
        this.copycatWheels$copycatBlock = id;
        copycatWheels$sync();
    }

    @Override @Nullable
    public ResourceLocation copycatWheels$getCopycatBlock() {
        return this.copycatWheels$copycatBlock;
    }

    @Override
    public void copycatWheels$setCopycatRotation(@Nullable final float[] xyzw) {
        if (xyzw == null || xyzw.length != 4) {
            if (this.copycatWheels$rotation == null) return;
            this.copycatWheels$rotation = null;
        } else {
            if (this.copycatWheels$rotation != null
                    && this.copycatWheels$rotation[0] == xyzw[0]
                    && this.copycatWheels$rotation[1] == xyzw[1]
                    && this.copycatWheels$rotation[2] == xyzw[2]
                    && this.copycatWheels$rotation[3] == xyzw[3]) return;
            this.copycatWheels$rotation = new float[]{xyzw[0], xyzw[1], xyzw[2], xyzw[3]};
        }
        copycatWheels$sync();
    }

    @Override @Nullable
    public float[] copycatWheels$getCopycatRotation() {
        return this.copycatWheels$rotation == null
                ? null
                : Arrays.copyOf(this.copycatWheels$rotation, 4);
    }

    @Override
    public void copycatWheels$setCopycatScale(final float scale) {
        final float clamped = Mth.clamp(scale, 0.1f, 10.0f);
        if (this.copycatWheels$scale == clamped) return;
        this.copycatWheels$scale = clamped;
        copycatWheels$sync();
    }

    @Override
    public float copycatWheels$getCopycatScale() {
        return this.copycatWheels$scale;
    }

    @Override
    public void copycatWheels$setFrictionMultiplier(final float m) {
        final float clamped = Mth.clamp(m, 0.0f, 2.0f);
        if (this.copycatWheels$frictionMultiplier == clamped) return;
        this.copycatWheels$frictionMultiplier = clamped;
        copycatWheels$sync();
    }

    @Override
    public float copycatWheels$getFrictionMultiplier() {
        return this.copycatWheels$frictionMultiplier;
    }

    @Override
    public void copycatWheels$setCopycatOffset(final float x, final float y, final float z) {
        final float cx = copycatWheels$snap(Mth.clamp(x, -OFFSET_LIMIT, OFFSET_LIMIT));
        final float cy = copycatWheels$snap(Mth.clamp(y, -OFFSET_LIMIT, OFFSET_LIMIT));
        final float cz = copycatWheels$snap(Mth.clamp(z, -OFFSET_LIMIT, OFFSET_LIMIT));
        if (Math.abs(this.copycatWheels$offsetX - cx) < EPS
                && Math.abs(this.copycatWheels$offsetY - cy) < EPS
                && Math.abs(this.copycatWheels$offsetZ - cz) < EPS) return;
        this.copycatWheels$offsetX = cx;
        this.copycatWheels$offsetY = cy;
        this.copycatWheels$offsetZ = cz;
        copycatWheels$sync();
    }

    @Unique
    private static float copycatWheels$snap(final float v) {
        return Math.round(v / OFFSET_STEP) * OFFSET_STEP;
    }

    @Override public float copycatWheels$getOffsetX() { return this.copycatWheels$offsetX; }
    @Override public float copycatWheels$getOffsetY() { return this.copycatWheels$offsetY; }
    @Override public float copycatWheels$getOffsetZ() { return this.copycatWheels$offsetZ; }

    @Override
    public void copycatWheels$setMaterial(@Nullable final BlockState state) {
        if (Objects.equals(this.copycatWheels$material, state)) return;
        this.copycatWheels$material = state;
        copycatWheels$sync();
    }

    @Override @Nullable
    public BlockState copycatWheels$getMaterial() {
        return this.copycatWheels$material;
    }

        @Unique
    private void copycatWheels$sync() {
        final WheelMountBlockEntity self = (WheelMountBlockEntity) (Object) this;
        self.setChanged();
        if (self instanceof final SmartBlockEntity smart) smart.sendData();

        self.requestModelDataUpdate();
        if (self.getLevel() != null) {
            self.getLevel().sendBlockUpdated(
                    self.getBlockPos(), self.getBlockState(), self.getBlockState(), 3);

            self.getLevel().getChunkSource().getLightEngine().checkBlock(self.getBlockPos());
        }
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void copycatWheels$write(
            final CompoundTag tag,
            final HolderLookup.Provider registries,
            final boolean clientPacket,
            final CallbackInfo ci
    ) {
        if (this.copycatWheels$copycatBlock != null) {
            tag.putString("CopycatWheelsBlock", this.copycatWheels$copycatBlock.toString());
        }
        if (this.copycatWheels$rotation != null) {
            final ListTag list = new ListTag();
            for (int i = 0; i < 4; i++) list.add(FloatTag.valueOf(this.copycatWheels$rotation[i]));
            tag.put("CopycatWheelsRotation", list);
        }
        if (this.copycatWheels$scale != 1.0f) {
            tag.putFloat("CopycatWheelsScale", this.copycatWheels$scale);
        }
        if (this.copycatWheels$frictionMultiplier != 1.0f) {
            tag.putFloat("CopycatWheelsFriction", this.copycatWheels$frictionMultiplier);
        }
        if (this.copycatWheels$offsetX != 0f || this.copycatWheels$offsetY != 0f || this.copycatWheels$offsetZ != 0f) {
            tag.putFloat("CopycatWheelsOffsetX", this.copycatWheels$offsetX);
            tag.putFloat("CopycatWheelsOffsetY", this.copycatWheels$offsetY);
            tag.putFloat("CopycatWheelsOffsetZ", this.copycatWheels$offsetZ);
        }
        if (this.copycatWheels$material != null) {
            tag.put("CopycatWheelsMaterial", NbtUtils.writeBlockState(this.copycatWheels$material));
        }
    }

    @Inject(method = "read", at = @At("TAIL"))
    private void copycatWheels$read(
            final CompoundTag tag,
            final HolderLookup.Provider registries,
            final boolean clientPacket,
            final CallbackInfo ci
    ) {
        final WheelMountBlockEntity self = (WheelMountBlockEntity) (Object) this;
        final BlockState prevMaterial = this.copycatWheels$material;

        if (tag.contains("CopycatWheelsBlock")) {
            this.copycatWheels$copycatBlock =
                    ResourceLocation.tryParse(tag.getString("CopycatWheelsBlock"));
        } else {
            this.copycatWheels$copycatBlock = null;
        }

        if (tag.contains("CopycatWheelsRotation", Tag.TAG_LIST)) {
            final ListTag list = tag.getList("CopycatWheelsRotation", Tag.TAG_FLOAT);
            if (list.size() == 4) {
                this.copycatWheels$rotation = new float[]{
                        list.getFloat(0), list.getFloat(1),
                        list.getFloat(2), list.getFloat(3)
                };
            } else {
                this.copycatWheels$rotation = null;
            }
        } else {
            this.copycatWheels$rotation = null;
        }

        this.copycatWheels$scale = tag.contains("CopycatWheelsScale")
                ? tag.getFloat("CopycatWheelsScale") : 1.0f;
        this.copycatWheels$frictionMultiplier = tag.contains("CopycatWheelsFriction")
                ? tag.getFloat("CopycatWheelsFriction") : 1.0f;
        this.copycatWheels$offsetX = tag.contains("CopycatWheelsOffsetX")
                ? tag.getFloat("CopycatWheelsOffsetX") : 0f;
        this.copycatWheels$offsetY = tag.contains("CopycatWheelsOffsetY")
                ? tag.getFloat("CopycatWheelsOffsetY") : 0f;
        this.copycatWheels$offsetZ = tag.contains("CopycatWheelsOffsetZ")
                ? tag.getFloat("CopycatWheelsOffsetZ") : 0f;

        if (tag.contains("CopycatWheelsMaterial")) {
            try {
                this.copycatWheels$material = NbtUtils.readBlockState(
                        self.blockHolderGetter(), tag.getCompound("CopycatWheelsMaterial"));
            } catch (final Throwable t) {
                this.copycatWheels$material = null;
            }
        } else {
            this.copycatWheels$material = null;
        }

        if (clientPacket && prevMaterial != this.copycatWheels$material) {
            self.requestModelDataUpdate();
            if (self.getLevel() != null) {
                self.getLevel().sendBlockUpdated(
                        self.getBlockPos(), self.getBlockState(), self.getBlockState(), 3);
            }
        }
    }
}