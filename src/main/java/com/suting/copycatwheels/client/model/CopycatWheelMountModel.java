package com.suting.copycatwheels.client.model;

import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import com.suting.copycatwheels.CopycatWheelAccess;
import dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
        return false; 
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
            //
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
            List<BakedQuad> candidates = null;
            if (hasMaterial && materialModel != null) {
                candidates = materialModel.getQuads(
                        material, originalQuad.getDirection(), rand, ModelData.EMPTY, renderType);
                if (candidates.isEmpty()) {
                    candidates = materialModel.getQuads(
                            material, null, rand, ModelData.EMPTY, renderType);
                }
            }

            if (candidates != null && !candidates.isEmpty()) {
                for (final BakedQuad candidate : candidates) {
                    BakedQuad processed = copycatWheels$replaceSpriteAndTint(
                            originalQuad, candidate.getSprite(), candidate.getTintIndex());
                    if (quarter != 0) processed = copycatWheels$rotateQuadY(processed, quarter);
                    result.add(processed);
                }
            } else {
                BakedQuad processed = originalQuad;
                if (quarter != 0) processed = copycatWheels$rotateQuadY(processed, quarter);
                result.add(processed);
            }
        }

        return result;
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

    private static BakedQuad copycatWheels$replaceSpriteAndTint(
            final BakedQuad quad, final TextureAtlasSprite newSprite, final int newTintIndex) {

        final int[] vertices = quad.getVertices().clone();
        final int stride = 8;
        final TextureAtlasSprite oldSprite = quad.getSprite();
        final float oldU0 = oldSprite.getU0();
        final float oldV0 = oldSprite.getV0();
        final float oldUSize = oldSprite.getU1() - oldU0;
        final float oldVSize = oldSprite.getV1() - oldV0;

        if (oldUSize <= 0.0F || oldVSize <= 0.0F) return quad;

        final float newU0 = newSprite.getU0();
        final float newV0 = newSprite.getV0();
        final float newUSize = newSprite.getU1() - newU0;
        final float newVSize = newSprite.getV1() - newV0;

        for (int i = 0; i < 4; i++) {
            final int base = i * 8;
            final float u = Float.intBitsToFloat(vertices[base + 4]);
            final float v = Float.intBitsToFloat(vertices[base + 5]);
            final float relU = (u - oldU0) / oldUSize;
            final float relV = (v - oldV0) / oldVSize;
            vertices[base + 4] = Float.floatToRawIntBits(newU0 + relU * newUSize);
            vertices[base + 5] = Float.floatToRawIntBits(newV0 + relV * newVSize);
        }

        return new BakedQuad(vertices, newTintIndex, quad.getDirection(), newSprite, quad.isShade());
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