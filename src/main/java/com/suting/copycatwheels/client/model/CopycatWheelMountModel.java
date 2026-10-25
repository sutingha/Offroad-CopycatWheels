package com.suting.copycatwheels.client.model;

import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.suting.copycatwheels.CopycatWheelAccess;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CopycatWheelMountModel extends BakedModelWrapperWithData {

    @Nullable
    private final BakedModel skinModel;

    public CopycatWheelMountModel(final BakedModel originalModel, @Nullable final BakedModel skinModel) {
        super(originalModel);
        this.skinModel = skinModel;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return super.useAmbientOcclusion();
    }

    @Override
    protected ModelData.Builder gatherModelData(
            final ModelData.Builder builder,
            final BlockAndTintGetter level,
            final BlockPos pos,
            final BlockState state,
            final ModelData blockEntityData
    ) {
        BlockState material = blockEntityData.get(CopycatWheelModelProperties.MATERIAL_PROPERTY);
        if (material == null || material.isAir()) {
            try {
                if (level.getBlockEntity(pos) instanceof final WheelMountBlockEntity mount
                        && mount instanceof final CopycatWheelAccess access) {
                    material = access.copycatWheels$getMaterial();
                }
            } catch (final Throwable ignored) {}
        }

        if (material != null && !material.isAir()) {
            builder.with(CopycatWheelModelProperties.MATERIAL_PROPERTY, material);
            try {
                if (material.getLightEmission(level, pos) > 0) {
                    builder.with(CopycatWheelModelProperties.EMISSIVE_PROPERTY, Boolean.TRUE);
                }
            } catch (final Throwable ignored) {}
        }

        return builder;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(final BlockState state, final RandomSource rand, final ModelData data) {
        final BlockState material = data.get(CopycatWheelModelProperties.MATERIAL_PROPERTY);
        if (material != null && !material.isAir()) {
            try {
                final RenderType matType = ItemBlockRenderTypes.getChunkRenderType(material);
                if (matType == RenderType.translucent()) {
                    return ChunkRenderTypeSet.of(RenderType.cutoutMipped(), RenderType.translucent());
                }
            } catch (final Throwable ignored) {}
        }
        return ChunkRenderTypeSet.of(RenderType.cutoutMipped());
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable final BlockState state,
            @Nullable final Direction side,
            final RandomSource rand,
            final ModelData data,
            @Nullable final RenderType renderType
    ) {
        final BlockState material = data.get(CopycatWheelModelProperties.MATERIAL_PROPERTY);
        boolean isTranslucentMat = false;
        if (material != null && !material.isAir()) {
            try {
                isTranslucentMat = ItemBlockRenderTypes.getChunkRenderType(material) == RenderType.translucent();
            } catch (final Throwable ignored) {}
        }

        if (renderType == RenderType.translucent()) {
            if (!isTranslucentMat) return List.of();
            return copycatWheels$getSkinQuads(state, side, rand, data, renderType);
        } else {
            final List<BakedQuad> result = new ArrayList<>(this.originalModel.getQuads(state, side, rand, data, renderType));

            if (isTranslucentMat && renderType != null) {
                return result;
            }
            result.addAll(copycatWheels$getSkinQuads(state, side, rand, data, renderType));
            return result;
        }
    }

    private List<BakedQuad> copycatWheels$getSkinQuads(
            @Nullable final BlockState state,
            @Nullable final Direction side,
            final RandomSource rand,
            final ModelData data,
            @Nullable final RenderType renderType
    ) {
        if (this.skinModel == null) return List.of();

        final List<BakedQuad> skinQuadsRaw = this.skinModel.getQuads(state, side, rand, data, renderType);
        if (skinQuadsRaw.isEmpty()) return List.of();

        final int quarter = copycatWheels$yQuarter(state);
        final BlockState material = data.get(CopycatWheelModelProperties.MATERIAL_PROPERTY);
        final boolean hasMaterial = material != null && !material.isAir();
        final boolean emissive = Boolean.TRUE.equals(data.get(CopycatWheelModelProperties.EMISSIVE_PROPERTY));

        BakedModel materialModel = null;
        if (hasMaterial) {
            try {
                materialModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(material);
            } catch (final Throwable ignored) {
                materialModel = null;
            }
        }
        final BakedModel finalMaterialModel = materialModel;

        final List<BakedQuad> skinQuads = new ArrayList<>(skinQuadsRaw.size());
        for (final BakedQuad q : skinQuadsRaw) {
            skinQuads.add(new BakedQuad(
                    q.getVertices().clone(),
                    q.getTintIndex(),
                    q.getDirection(),
                    q.getSprite(),
                    q.isShade()));
        }

        if (emissive) {
            QuadTransformers.settingMaxEmissivity().processInPlace(skinQuads);
        }

        final List<BakedQuad> result = new ArrayList<>();

        for (final BakedQuad originalQuad : skinQuads) {
            BakedQuad rotated = originalQuad;
            if (quarter != 0) {
                rotated = copycatWheels$rotateQuadY(rotated, quarter);
            }

            final List<BakedQuad> candidates = (hasMaterial && finalMaterialModel != null)
                    ? copycatWheels$findMaterialQuads(
                            finalMaterialModel, material, rotated.getDirection(), rand, renderType)
                    : List.of();

            if (!candidates.isEmpty()) {
                for (final BakedQuad candidate : candidates) {
                    result.add(copycatWheels$cropSkinUv(rotated, candidate));
                }
            } else {
                result.add(rotated);
            }
        }

        return result;
    }

    private static List<BakedQuad> copycatWheels$findMaterialQuads(
            final BakedModel materialModel,
            final BlockState material,
            final Direction dir,
            final RandomSource rand,
            @Nullable final RenderType renderType
    ) {
        if (renderType != null) {
            final List<BakedQuad> quads = materialModel.getQuads(material, dir, rand, ModelData.EMPTY, renderType);
            if (!quads.isEmpty()) return quads;
        }
        final List<BakedQuad> quads = materialModel.getQuads(material, dir, rand, ModelData.EMPTY, null);
        if (!quads.isEmpty()) return quads;
        final List<BakedQuad> all = materialModel.getQuads(material, null, rand, ModelData.EMPTY, renderType);
        final List<BakedQuad> filtered = new ArrayList<>();
        for (final BakedQuad q : all) {
            if (q.getDirection() == dir) filtered.add(q);
        }
        return filtered;
    }

    private static int copycatWheels$yQuarter(@Nullable final BlockState state) {
        if (state == null) return 0;
        if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return 0;

        return switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    private static BakedQuad copycatWheels$cropSkinUv(
            final BakedQuad skinQuad,
            final BakedQuad materialQuad
    ) {
        final int[] skinVerts = skinQuad.getVertices().clone();
        final int[] matVerts = materialQuad.getVertices();

        final float[] mPx = new float[4], mPy = new float[4], mPz = new float[4];
        final float[] mU = new float[4], mV = new float[4];
        for (int i = 0; i < 4; i++) {
            final int base = i * 8;
            mPx[i] = Float.intBitsToFloat(matVerts[base]);
            mPy[i] = Float.intBitsToFloat(matVerts[base + 1]);
            mPz[i] = Float.intBitsToFloat(matVerts[base + 2]);
            mU[i] = Float.intBitsToFloat(matVerts[base + 4]);
            mV[i] = Float.intBitsToFloat(matVerts[base + 5]);
        }

        final Direction dir = materialQuad.getDirection();
        final Axis axisH;
        final Axis axisV;
        switch (dir.getAxis()) {
            case X -> { axisH = Axis.Z; axisV = Axis.Y; }
            case Y -> { axisH = Axis.X; axisV = Axis.Z; }
            default -> { axisH = Axis.X; axisV = Axis.Y; }
        }

        final float e1H = (float) axisH.choose(mPx[1] - mPx[0], mPy[1] - mPy[0], mPz[1] - mPz[0]);
        final float e1V = (float) axisV.choose(mPx[1] - mPx[0], mPy[1] - mPy[0], mPz[1] - mPz[0]);
        final float e2H = (float) axisH.choose(mPx[3] - mPx[0], mPy[3] - mPy[0], mPz[3] - mPz[0]);
        final float e2V = (float) axisV.choose(mPx[3] - mPx[0], mPy[3] - mPy[0], mPz[3] - mPz[0]);

        final float det = e1H * e2V - e1V * e2H;
        if (Math.abs(det) < 1e-8f) {
            return new BakedQuad(
                    skinVerts,
                    materialQuad.getTintIndex(),
                    skinQuad.getDirection(),
                    materialQuad.getSprite(),
                    skinQuad.isShade()
            );
        }

        for (int i = 0; i < 4; i++) {
            final int base = i * 8;
            final float sx = Float.intBitsToFloat(skinVerts[base]);
            final float sy = Float.intBitsToFloat(skinVerts[base + 1]);
            final float sz = Float.intBitsToFloat(skinVerts[base + 2]);

            final float dH = (float) axisH.choose(sx - mPx[0], sy - mPy[0], sz - mPz[0]);
            final float dV = (float) axisV.choose(sx - mPx[0], sy - mPy[0], sz - mPz[0]);

            final float s = (dH * e2V - dV * e2H) / det;
            final float t = (e1H * dV - e1V * dH) / det;

            final float u = (1 - s) * (1 - t) * mU[0]
                    + s * (1 - t) * mU[1]
                    + s * t * mU[2]
                    + (1 - s) * t * mU[3];
            final float v = (1 - s) * (1 - t) * mV[0]
                    + s * (1 - t) * mV[1]
                    + s * t * mV[2]
                    + (1 - s) * t * mV[3];

            skinVerts[base + 4] = Float.floatToRawIntBits(u);
            skinVerts[base + 5] = Float.floatToRawIntBits(v);
        }

        return new BakedQuad(
                skinVerts,
                materialQuad.getTintIndex(),
                skinQuad.getDirection(),
                materialQuad.getSprite(),
                materialQuad.isShade()
        );
    }

    private static BakedQuad copycatWheels$rotateQuadY(final BakedQuad quad, final int quarter) {
        if (quarter == 0) return quad;

        final float rad = (float) Math.toRadians(quarter * 90);
        final float cos = (float) Math.cos(rad);
        final float sin = (float) Math.sin(rad);
        final int[] vertices = quad.getVertices().clone();

        for (int i = 0; i < 4; i++) {
            final int base = i * 8;

            final float x = Float.intBitsToFloat(vertices[base]) - 0.5F;
            final float z = Float.intBitsToFloat(vertices[base + 2]) - 0.5F;
            final float nx = x * cos - z * sin;
            final float nz = x * sin + z * cos;
            vertices[base] = Float.floatToRawIntBits(nx + 0.5F);
            vertices[base + 2] = Float.floatToRawIntBits(nz + 0.5F);

            if (vertices.length >= (i + 1) * 8) {
                final int normal = vertices[base + 7];
                final float normX = (byte) (normal & 0xFF) / 127.0F;
                final float normY = (byte) ((normal >> 8) & 0xFF) / 127.0F;
                final float normZ = (byte) ((normal >> 16) & 0xFF) / 127.0F;

                final float rnx = normX * cos - normZ * sin;
                final float rnz = normX * sin + normZ * cos;

                vertices[base + 7] = ((byte) (rnx * 127) & 0xFF)
                        | (((byte) (normY * 127) & 0xFF) << 8)
                        | (((byte) (rnz * 127) & 0xFF) << 16);
            }
        }

        final Direction newDir = copycatWheels$rotateDirection(quad.getDirection(), quarter);
        return new BakedQuad(vertices, quad.getTintIndex(), newDir, quad.getSprite(), quad.isShade());
    }

    private static Direction copycatWheels$rotateDirection(final Direction dir, final int quarter) {
        if (dir.getAxis() == Axis.Y) return dir;
        Direction result = dir;
        for (int i = 0; i < quarter; i++) result = result.getClockWise();
        return result;
    }
}