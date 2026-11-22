package com.suting.copycatwheels.content.block.copycat_wheel_mount_ii;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.suting.copycatwheels.client.model.CopycatWheelModelProperties;
import com.suting.copycatwheels.core.CopycatWheelAccess;
import com.suting.copycatwheels.registry.CopycatWheelsBlockEntityTypes;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.math.OrientedBoundingBox3d;
import dev.ryanhcode.sable.api.physics.force.ForceTotal;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.physics.mass.MassData;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinterface.clip_overwrite.ClipContextExtension;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CopycatWheelMountIIBlockEntity extends KineticBlockEntity implements BlockEntitySubLevelActor, Clearable, CopycatWheelAccess {
    public static final Collection<CopycatWheelMountIIBlockEntity> QUEUED_MOUNTS = new ObjectOpenHashSet<>();
    private final CopycatWheelMountIIInventory inventory;
    private ScrollValueBehaviour strength;

    private double leftExtension = 0.5, lastLeftExtension = 0.5;
    private double rightExtension = 0.5, lastRightExtension = 0.5;
    private double leftAngle, lastLeftAngle, rightAngle, lastRightAngle;
    private double leftAngularVelocity, rightAngularVelocity;
    private double leftFriction = 1.0, rightFriction = 1.0;
    private double chasingYaw, lastChasingYaw;
    private boolean visualSuspensionHidden;
    private final Vector3d leftForcePos = new Vector3d(), rightForcePos = new Vector3d();
    private final Vector3d leftForce = new Vector3d(), rightForce = new Vector3d();
    private final ForceTotal forceTotal = new ForceTotal();

    public double lateralOffset, longitudinalOffset, heightOffset;
    private double visLateral, lastVisLateral, visLongitudinal, lastVisLongitudinal, visHeight, lastVisHeight;
    private boolean visualOffsetsInitialized;

    @Nullable private ResourceLocation leftCopycatBlock = null;
    @Nullable private ResourceLocation rightCopycatBlock = null;
    @Nullable private float[] leftRotation = null;
    @Nullable private float[] rightRotation = null;
    private float copycatScale = 1.0f;
    private float frictionMultiplier = 1.0f;
    private float offsetX = 0f, offsetY = 0f, offsetZ = 0f;

    @Nullable private BlockState copycatWheels$material = null;

    public CopycatWheelMountIIBlockEntity(BlockPos pos, BlockState state) {
        super(CopycatWheelsBlockEntityTypes.COPYCAT_WHEEL_MOUNT_II.get(), pos, state);
        this.inventory = new CopycatWheelMountIIInventory(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        copycatWheels$updateAuxLightNow();
    }

    public void saveToItem(ItemStack stack) {
        // no-op
    }

    public static void applyAllBatchedForces(Object system, double timeStep) {
        for (CopycatWheelMountIIBlockEntity be : QUEUED_MOUNTS) {
            if (!be.isRemoved()) be.applyBatchedForces();
        }
        QUEUED_MOUNTS.clear();
    }

    private void applyBatchedForces() {
        SubLevel subLevel = Sable.HELPER.getContaining(this);
        if (subLevel != null) {
            RigidBodyHandle handle = RigidBodyHandle.of((ServerSubLevel)subLevel);
            handle.applyForcesAndReset(this.forceTotal);
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(this.strength = new SuspensionStrengthValueBehaviour(
                Component.translatable("copycatwheels.scroll_option.suspension_strength"),
                this, new SuspensionStrengthValueBox(0)));
        this.strength.value = 10;
    }

    public ItemStack getLeftWheel() { return this.inventory.getLeftWheel(); }
    public ItemStack getRightWheel() { return this.inventory.getRightWheel(); }
    public CopycatWheelMountIIInventory getInventory() { return this.inventory; }
    public void onStackChanged() { this.invalidateRenderBoundingBox(); }
    public void clearContent() { this.inventory.clearContent(); this.onStackChanged(); }

    @Override
    public ModelData getModelData() {
        if (this.copycatWheels$material != null && !this.copycatWheels$material.isAir()) {
            return ModelData.builder().with(CopycatWheelModelProperties.MATERIAL_PROPERTY, this.copycatWheels$material).build();
        }
        return super.getModelData();
    }

    public boolean isCopycatWheel(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return "offroad".equals(id.getNamespace()) && id.getPath().startsWith("copycat_wheel");
    }

    @Override
    public void sable$physicsTick(ServerSubLevel subLevel, RigidBodyHandle handle, double timeStep) {
        boolean left = !getLeftWheel().isEmpty() && getLeftWheel().get(OffroadDataComponents.TIRE) != null;
        boolean right = !getRightWheel().isEmpty() && getRightWheel().get(OffroadDataComponents.TIRE) != null;
        if (left || right) {
            MassData massData = subLevel.getMassTracker();
            Pose3d pose = subLevel.logicalPose();
            Direction facing = getBlockState().getValue(CopycatWheelMountIIBlock.HORIZONTAL_FACING);
            if (left) applyWheelPhysics(subLevel, massData, pose, facing, getLeftWheel(), true, timeStep);
            if (right) applyWheelPhysics(subLevel, massData, pose, facing, getRightWheel(), false, timeStep);
            QUEUED_MOUNTS.add(this);
        }
    }

    private void applyWheelPhysics(ServerSubLevel subLevel, MassData massData, Pose3d pose, Direction facing, ItemStack wheelStack, boolean leftSide, double timeStep) {
        TireLike tire = wheelStack.get(OffroadDataComponents.TIRE);
        if (tire == null) return;

        float radius = tire.radius();
        float frictionMult = 1.0f;
        if (isCopycatWheel(wheelStack)) {
            ResourceLocation sideBlock = leftSide ? leftCopycatBlock : rightCopycatBlock;
            if (sideBlock != null) {
                frictionMult = this.frictionMultiplier;
            }
        }

        Vec3 wheelPos = getWheelPosition(facing, leftSide);
        Vector3d forcePos = leftSide ? leftForcePos : rightForcePos;
        Vector3d force = leftSide ? leftForce : rightForce;
        forcePos.set(wheelPos.x, wheelPos.y, wheelPos.z);

        double normalMass = 1.0 / massData.getInverseNormalMass(forcePos, OrientedBoundingBox3d.UP);
        double effectiveStrength = strength == null ? 10.0 : strength.getValue();
        double normalMassScaling = Math.min(normalMass / effectiveStrength, 1.0) * 10.0;
        double strengthMul = effectiveStrength * normalMassScaling * 2.0;
        double springStrength = effectiveStrength * normalMassScaling * 40.0;
        double dampingStrength = effectiveStrength * normalMassScaling;

        double steeringYaw = getSteeringYaw(facing);
        Vec3i sideAxis = facing.getCounterClockWise().getNormal();
        Vector3dc sideD = getRotatedWheelAxis(sideAxis, steeringYaw);
        Vec3i rollingAxis = facing.getNormal();
        Vector3dc normalD = getRotatedWheelAxis(rollingAxis, steeringYaw);

        TerrainCastResult terrain = computeMaxExtensionToTerrain(facing, wheelPos, sideD, pose);
        double maxExtension = terrain.maxExtension();

        if (leftSide) this.leftExtension = Mth.lerp(1.0, this.leftExtension, maxExtension);
        else this.rightExtension = Mth.lerp(1.0, this.rightExtension, maxExtension);

        if (maxExtension > 0.65 + radius + 0.25) {
            if (leftSide) { this.leftExtension = 0.65; }
            else { this.rightExtension = 0.65; }
        } else {
            double extension = leftSide ? this.leftExtension : this.rightExtension;
            double distance = 0.108333334F + extension;
            double springLength = Mth.clamp(distance - radius, 0.0, 0.65);
            Vector3d velocity = Sable.HELPER.getVelocity(this.level, JOMLConversion.toJOML(wheelPos));
            Vector3d localVelocity = pose.transformNormalInverse(velocity);
            double dampingForce = -localVelocity.y * dampingStrength;
            double springForce = ((0.65 - springLength) * springStrength + dampingForce) * timeStep;

            Vec3i rayHitNormal = terrain.normal().getNormal();
            Vec3 localForce = new Vec3(springForce * rayHitNormal.getX(), springForce * rayHitNormal.getY(), springForce * rayHitNormal.getZ());
            if (terrain.subLevel() != null) localForce = terrain.subLevel().logicalPose().transformNormal(localForce);
            localForce = pose.transformNormalInverse(localForce);
            force.set(localForce.x, localForce.y, localForce.z);

            double friction = terrain.minInteractingBlock() == null ? 1.0 : fudgeFriction(PhysicsBlockPropertyHelper.getFriction(this.level.getBlockState(terrain.minInteractingBlock())));
            friction *= frictionMult;
            if (leftSide) this.leftFriction = friction; else this.rightFriction = friction;

            double brakeStrength = getBrakeStrength();
            double surfaceBraking = Math.min(friction, 1.0);
            double brakingFrictionStrength = (0.075 + brakeStrength * 0.3) * surfaceBraking;
            float kineticSpeed = facing.getAxis() == Axis.X ? this.getSpeed() : -this.getSpeed();
            double currentRollingSpeed = localVelocity.dot(normalD);
            double targetRollingSpeed = kineticSpeed * ((Math.PI * 2) * radius / 60.0);
            double driveForce = kineticSpeed * (1.0 - brakeStrength) * surfaceBraking * 1.75 * timeStep;
            if (kineticSpeed * currentRollingSpeed > 0.0 && Math.abs(currentRollingSpeed) >= Math.abs(targetRollingSpeed)) driveForce = 0.0;

            force.fma(localVelocity.dot(normalD) * -brakingFrictionStrength * strengthMul * timeStep + driveForce, normalD);
            force.fma(localVelocity.dot(sideD) * -0.6 * friction * strengthMul * timeStep, sideD);
            this.forceTotal.applyImpulseAtPoint(subLevel, forcePos, force);
        }
    }

    private Vec3 getWheelPosition(Direction facing, boolean leftSide) {
        Direction side = leftSide ? facing.getCounterClockWise() : facing.getClockWise();
        return getBlockPos().relative(side).getCenter().add(offsetVector(facing, leftSide ? -lateralOffset : lateralOffset, longitudinalOffset, heightOffset));
    }

    private static Vec3 offsetVector(Direction facing, double lateral, double longitudinal, double height) {
        Vec3 lateralAxis = Vec3.atLowerCornerOf(facing.getClockWise().getNormal());
        Vec3 longitudinalAxis = Vec3.atLowerCornerOf(facing.getNormal());
        return lateralAxis.scale(lateral).add(longitudinalAxis.scale(longitudinal)).add(0.0, height, 0.0);
    }

    public Vec3 getLerpedRenderOffset(float partialTick, boolean leftSide) {
        Direction facing = getBlockState().getValue(CopycatWheelMountIIBlock.HORIZONTAL_FACING);
        double lat = Mth.lerp(partialTick, lastVisLateral, visLateral);
        double lon = Mth.lerp(partialTick, lastVisLongitudinal, visLongitudinal);
        double h = Mth.lerp(partialTick, lastVisHeight, visHeight);
        return offsetVector(facing, leftSide ? -lat : lat, lon, h);
    }

    public void tuningChanged() {
        this.setChanged();
        if (level != null && !level.isClientSide) {
            sendData();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public double copycatWheels$adjustLateralOffset(int dir) { lateralOffset = Mth.clamp(Math.round((lateralOffset + dir * 0.125) / 0.125) * 0.125, -1.0, 1.5); tuningChanged(); return lateralOffset; }
    public double copycatWheels$adjustLongitudinalOffset(int dir) { longitudinalOffset = Mth.clamp(Math.round((longitudinalOffset + dir * 0.125) / 0.125) * 0.125, -1.0, 1.5); tuningChanged(); return longitudinalOffset; }
    public double copycatWheels$adjustHeightOffset(int dir) { heightOffset = Mth.clamp(Math.round((heightOffset + dir * 0.125) / 0.125) * 0.125, -0.75, 0.75); tuningChanged(); return heightOffset; }
    public double copycatWheels$getLerpedLateralOffset(float pt) { return Mth.lerp(pt, lastVisLateral, visLateral); }
    public double copycatWheels$getLerpedLongitudinalOffset(float pt) { return Mth.lerp(pt, lastVisLongitudinal, visLongitudinal); }
    public double copycatWheels$getLerpedHeightOffset(float pt) { return Mth.lerp(pt, lastVisHeight, visHeight); }
    public double copycatWheels$getLerpedYaw(float pt) { return Mth.lerp(pt, lastChasingYaw, chasingYaw); }
    public int copycatWheels$getClientSteeringSignalLeft() { return 0; }
    public int copycatWheels$getClientSteeringSignalRight() { return 0; }
    public void copycatWheels$resetTuning() { lateralOffset = 0; longitudinalOffset = 0; heightOffset = 0; tuningChanged(); }
    public double copycatWheels$adjustTuning(String key, int direction) { return 1.0; }
    public double copycatWheels$getTuning(String key) { return 1.0; }

    public boolean copycatWheels$isVisualSuspensionHidden() { return visualSuspensionHidden; }
    public void copycatWheels$toggleVisualSuspensionHidden() { visualSuspensionHidden = !visualSuspensionHidden; setChanged(); sendData(); }

    @Nullable public ResourceLocation getLeftCopycatBlock() { return leftCopycatBlock; }
    @Nullable public ResourceLocation getRightCopycatBlock() { return rightCopycatBlock; }
    public void setLeftCopycatBlock(@Nullable ResourceLocation id) { this.leftCopycatBlock = id; tuningChanged(); }
    public void setRightCopycatBlock(@Nullable ResourceLocation id) { this.rightCopycatBlock = id; tuningChanged(); }

    @Nullable public float[] getLeftRotation() { return leftRotation; }
    @Nullable public float[] getRightRotation() { return rightRotation; }
    public void setLeftRotation(@Nullable float[] q) { this.leftRotation = q; tuningChanged(); }
    public void setRightRotation(@Nullable float[] q) { this.rightRotation = q; tuningChanged(); }

    public float getCopycatScale() { return copycatScale; }
    public void setCopycatScale(float s) { this.copycatScale = s; tuningChanged(); }
    public float getFrictionMultiplier() { return frictionMultiplier; }
    public void setFrictionMultiplier(float m) { this.frictionMultiplier = m; tuningChanged(); }

    private static double fudgeFriction(double realValue) { return realValue < 1.0 ? 0.1 + 0.9 * realValue : realValue; }

    public double getLerpedSteeringYaw(float partialTick) { return Mth.lerp(partialTick, lastChasingYaw, chasingYaw); }
    public double getSteeringYawForRendering() { return chasingYaw; }

    private double getSteeringYaw(Direction facing) {
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        int leftSignal = level.getSignal(getBlockPos().relative(left), left);
        int rightSignal = level.getSignal(getBlockPos().relative(right), right);
        return -((leftSignal - rightSignal) / 15.0) * (Math.PI / 6);
    }

    public double getBrakeStrength() {
        if (this.level == null) return 0.0;
        int signal = Math.max(
                this.level.getSignal(this.getBlockPos().above(), Direction.UP),
                this.level.getSignal(this.getBlockPos(), Direction.UP)
        );
        return signal / 15.0;
    }

    private Vector3dc getRotatedWheelAxis(Vec3i axis, double steeringYaw) { return new Vector3d(axis.getX(), axis.getY(), axis.getZ()).rotateY(steeringYaw); }

    @Override
    public void tick() {
        super.tick();
        lastLeftExtension = leftExtension; lastRightExtension = rightExtension;
        lastLeftAngle = leftAngle; lastRightAngle = rightAngle;

        lastChasingYaw = chasingYaw;
        chasingYaw = Mth.lerp(0.4, chasingYaw, getSteeringYaw(getBlockState().getValue(CopycatWheelMountIIBlock.HORIZONTAL_FACING)));

        if (level.isClientSide) {
            lastVisLateral = visLateral; lastVisLongitudinal = visLongitudinal; lastVisHeight = visHeight;
            visLateral = Mth.lerp(0.5, visLateral, lateralOffset);
            visLongitudinal = Mth.lerp(0.5, visLongitudinal, longitudinalOffset);
            visHeight = Mth.lerp(0.5, visHeight, heightOffset);
            updateClientWheel(true);
            updateClientWheel(false);
        }
    }

    private void updateClientWheel(boolean leftSide) {
        ItemStack stack = leftSide ? getLeftWheel() : getRightWheel();
        TireLike tire = stack.get(OffroadDataComponents.TIRE);
        if (tire == null) {
            if (leftSide) { leftAngle = 0; leftExtension = Mth.lerp(0.6, leftExtension, 0.5); }
            else { rightAngle = 0; rightExtension = Mth.lerp(0.6, rightExtension, 0.5); }
            return;
        }
        Direction facing = getBlockState().getValue(CopycatWheelMountIIBlock.HORIZONTAL_FACING);
        if (level != null) {
            SubLevel subLevel = Sable.HELPER.getContaining(this);
            if (subLevel == null) {
                if (leftSide) leftExtension = Mth.lerp(0.7, leftExtension, 0.65);
                else rightExtension = Mth.lerp(0.7, rightExtension, 0.65);
            } else {
                Vec3 wheelPos = getWheelPosition(facing, leftSide);
                Vector3dc sideD = getRotatedWheelAxis(facing.getCounterClockWise().getNormal(), chasingYaw);
                TerrainCastResult terrain = computeMaxExtensionToTerrain(facing, wheelPos, sideD, subLevel.logicalPose());
                double targetExt = Mth.clamp(terrain.maxExtension() - tire.radius(), -0.45, 0.65);
                if (leftSide) leftExtension = Mth.lerp(0.7, leftExtension, targetExt);
                else rightExtension = Mth.lerp(0.7, rightExtension, targetExt);
            }
        }

        double brakeFactor = 1.0 - getBrakeStrength();

        float speed = facing.getAxis() == Axis.X ? -getSpeed() : getSpeed();
        double attemptedVel = Mth.lerp(0.2,
                leftSide ? leftAngularVelocity : rightAngularVelocity,
                speed * brakeFactor * Math.PI * 2.0 / 60.0 / 20.0);
        if (leftSide) { leftAngularVelocity = attemptedVel; leftAngle += leftAngularVelocity; }
        else { rightAngularVelocity = attemptedVel; rightAngle += rightAngularVelocity; }
    }

    public float getLerpedLeftAngle(float pt) { return (float)Mth.lerp(pt, lastLeftAngle, leftAngle); }
    public float getLerpedRightAngle(float pt) { return (float)Mth.lerp(pt, lastRightAngle, rightAngle); }
    public double getLerpedLeftExtension(float pt) { return Mth.lerp(pt, lastLeftExtension, leftExtension); }
    public double getLerpedRightExtension(float pt) { return Mth.lerp(pt, lastRightExtension, rightExtension); }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.put("Wheels", inventory.write(registries));
        tag.putDouble("TracksLateralOffset", lateralOffset);
        tag.putDouble("TracksLongitudinalOffset", longitudinalOffset);
        tag.putDouble("TracksHeightOffset", heightOffset);
        tag.putBoolean("TracksVisualSuspensionHidden", visualSuspensionHidden);

        if (leftCopycatBlock != null) tag.putString("LeftCopycatBlock", leftCopycatBlock.toString());
        if (rightCopycatBlock != null) tag.putString("RightCopycatBlock", rightCopycatBlock.toString());
        if (leftRotation != null) {
            net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
            for (int i = 0; i < 4; i++) list.add(net.minecraft.nbt.FloatTag.valueOf(leftRotation[i]));
            tag.put("LeftRotation", list);
        }
        if (rightRotation != null) {
            net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
            for (int i = 0; i < 4; i++) list.add(net.minecraft.nbt.FloatTag.valueOf(rightRotation[i]));
            tag.put("RightRotation", list);
        }
        tag.putFloat("CopycatScale", copycatScale);
        tag.putFloat("FrictionMultiplier", frictionMultiplier);

        tag.putFloat("OffsetX", offsetX);
        tag.putFloat("OffsetY", offsetY);
        tag.putFloat("OffsetZ", offsetZ);

        if (copycatWheels$material != null) tag.put("CopycatWheelsMaterial", NbtUtils.writeBlockState(copycatWheels$material));

        super.write(tag, registries, clientPacket);
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        BlockState prevMaterial = this.copycatWheels$material;

        inventory.suppressUpdate = true;
        if (tag.contains("Wheels")) inventory.read(registries, tag.getCompound("Wheels"));
        inventory.suppressUpdate = false;
        onStackChanged();

        if (tag.contains("TracksLateralOffset")) lateralOffset = tag.getDouble("TracksLateralOffset");
        if (tag.contains("TracksLongitudinalOffset")) longitudinalOffset = tag.getDouble("TracksLongitudinalOffset");
        if (tag.contains("TracksHeightOffset")) heightOffset = tag.getDouble("TracksHeightOffset");
        visualSuspensionHidden = tag.getBoolean("TracksVisualSuspensionHidden");

        leftCopycatBlock = tag.contains("LeftCopycatBlock") ? ResourceLocation.tryParse(tag.getString("LeftCopycatBlock")) : null;
        rightCopycatBlock = tag.contains("RightCopycatBlock") ? ResourceLocation.tryParse(tag.getString("RightCopycatBlock")) : null;

        if (tag.contains("LeftRotation", net.minecraft.nbt.Tag.TAG_LIST)) {
            net.minecraft.nbt.ListTag list = tag.getList("LeftRotation", net.minecraft.nbt.Tag.TAG_FLOAT);
            if (list.size() == 4) leftRotation = new float[]{list.getFloat(0), list.getFloat(1), list.getFloat(2), list.getFloat(3)};
        } else leftRotation = null;

        if (tag.contains("RightRotation", net.minecraft.nbt.Tag.TAG_LIST)) {
            net.minecraft.nbt.ListTag list = tag.getList("RightRotation", net.minecraft.nbt.Tag.TAG_FLOAT);
            if (list.size() == 4) rightRotation = new float[]{list.getFloat(0), list.getFloat(1), list.getFloat(2), list.getFloat(3)};
        } else rightRotation = null;

        copycatScale = tag.contains("CopycatScale") ? tag.getFloat("CopycatScale") : 1.0f;
        frictionMultiplier = tag.contains("FrictionMultiplier") ? tag.getFloat("FrictionMultiplier") : 1.0f;

        offsetX = tag.contains("OffsetX") ? tag.getFloat("OffsetX") : 0f;
        offsetY = tag.contains("OffsetY") ? tag.getFloat("OffsetY") : 0f;
        offsetZ = tag.contains("OffsetZ") ? tag.getFloat("OffsetZ") : 0f;

        if (tag.contains("CopycatWheelsMaterial")) {
            try { copycatWheels$material = NbtUtils.readBlockState(blockHolderGetter(), tag.getCompound("CopycatWheelsMaterial")); } catch (Throwable ignored) { copycatWheels$material = null; }
        } else copycatWheels$material = null;

        if (!visualOffsetsInitialized) {
            visualOffsetsInitialized = true;
            visLateral = lastVisLateral = lateralOffset;
            visLongitudinal = lastVisLongitudinal = longitudinalOffset;
            visHeight = lastVisHeight = heightOffset;
        }

        super.read(tag, registries, clientPacket);

        if (!Objects.equals(prevMaterial, this.copycatWheels$material)) {
            this.requestModelDataUpdate();
            if (this.level != null) {
                this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
                this.level.getChunkSource().getLightEngine().checkBlock(this.getBlockPos());
            }
        }
    }

    private void copycatWheels$updateAuxLightNow() {
        if (this.level == null) return;
        final AuxiliaryLightManager lm = this.level.getAuxLightManager(this.getBlockPos());
        if (lm == null) return;
        final int light = (this.copycatWheels$material != null && !this.copycatWheels$material.isAir())
                ? this.copycatWheels$material.getLightEmission(this.level, this.getBlockPos())
                : 0;
        lm.setLightAt(this.getBlockPos(), light);
    }

    @Override
    public void copycatWheels$setCopycatBlock(@Nullable ResourceLocation id) {
        this.leftCopycatBlock = id;
        this.rightCopycatBlock = id;
        tuningChanged();
    }

    @Override @Nullable
    public ResourceLocation copycatWheels$getCopycatBlock() {
        return leftCopycatBlock != null ? leftCopycatBlock : rightCopycatBlock;
    }

    @Override
    public void copycatWheels$setCopycatRotation(@Nullable float[] xyzw) {
        this.leftRotation = xyzw == null ? null : xyzw.clone();
        this.rightRotation = xyzw == null ? null : xyzw.clone();
        tuningChanged();
    }

    @Override @Nullable
    public float[] copycatWheels$getCopycatRotation() {
        if (leftRotation != null) return leftRotation.clone();
        if (rightRotation != null) return rightRotation.clone();
        return null;
    }

    @Override public void copycatWheels$setCopycatScale(float scale) { this.copycatScale = scale; tuningChanged(); }
    @Override public float copycatWheels$getCopycatScale() { return copycatScale; }
    @Override public void copycatWheels$setFrictionMultiplier(float m) { this.frictionMultiplier = m; tuningChanged(); }
    @Override public float copycatWheels$getFrictionMultiplier() { return frictionMultiplier; }
    @Override public void copycatWheels$setCopycatOffset(float x, float y, float z) { this.offsetX = x; this.offsetY = y; this.offsetZ = z; tuningChanged(); }
    @Override public float copycatWheels$getOffsetX() { return offsetX; }
    @Override public float copycatWheels$getOffsetY() { return offsetY; }
    @Override public float copycatWheels$getOffsetZ() { return offsetZ; }

    @Override
    public void copycatWheels$setMaterial(@Nullable BlockState state) {
        if (Objects.equals(this.copycatWheels$material, state)) return;

        this.copycatWheels$material = state;
        this.setChanged();

        if (this.level != null) {
            if (!this.level.isClientSide) {
                this.sendData();
            }
            this.requestModelDataUpdate();
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
            this.level.getChunkSource().getLightEngine().checkBlock(this.getBlockPos());
            copycatWheels$updateAuxLightNow();
        }
    }
    @Override @Nullable public BlockState copycatWheels$getMaterial() { return copycatWheels$material; }

    private TerrainCastResult computeMaxExtensionToTerrain(Direction facing, Vec3 wheelPosCenter, Vector3dc normalD, Pose3dc pose) {
        double minExtension = 5.0; Direction minNormal = Direction.UP; SubLevel minHitSubLevel = null; BlockPos minInteractingBlock = null;
        for (int i = -1; i <= 1; i++) {
            Vec3 localPosO = wheelPosCenter.add(JOMLConversion.toMojang(normalD).scale(i));
            ClipContext clip = new ClipContext(localPosO, localPosO.subtract(0, 5, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
            ((ClipContextExtension)clip).sable$setIgnoredSubLevel(Sable.HELPER.getContaining(this));
            BlockHitResult clipResult = level.clip(clip);
            if (clipResult.getType() != HitResult.Type.MISS) {
                SubLevel hitSubLevel = Sable.HELPER.getContaining(level, clipResult.getLocation());
                Vec3 localHitPos = pose.transformPositionInverse(hitSubLevel == null ? clipResult.getLocation() : hitSubLevel.logicalPose().transformPosition(clipResult.getLocation()));
                if (!(localHitPos.y > wheelPosCenter.y) && !(localPosO.distanceTo(localHitPos) < 0.05)) {
                    double dist = wheelPosCenter.y - localHitPos.y;
                    if (dist > 1e-5) {
                        Direction dir = clipResult.getDirection();
                        Vector3d hitNormal = new Vector3d(dir.getStepX(), dir.getStepY(), dir.getStepZ());
                        if (hitSubLevel != null) hitSubLevel.logicalPose().transformNormal(hitNormal);
                        pose.transformNormalInverse(hitNormal);
                        if (!(hitNormal.dot(0, 1, 0) < 0.5) && dist < minExtension) {
                            minExtension = dist; minNormal = dir; minHitSubLevel = hitSubLevel; minInteractingBlock = clipResult.getBlockPos();
                        }
                    }
                }
            }
        }
        return new TerrainCastResult(minExtension, minNormal, minHitSubLevel, minInteractingBlock);
    }

    private record TerrainCastResult(double maxExtension, @NotNull Direction normal, @Nullable SubLevel subLevel, @Nullable BlockPos minInteractingBlock) {}

    private static class SuspensionStrengthValueBehaviour extends ScrollValueBehaviour {
        public SuspensionStrengthValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) { super(label, be, slot); this.between(5, 180); }
        @Override public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) { return new ValueSettingsBoard(this.label, 180, 20, List.of(Component.translatable("copycatwheels.scroll_option.suspension_strength_label")), new ValueSettingsFormatter(ValueSettings::format)); }
    }

    private static final class SuspensionStrengthValueBox extends ValueBoxTransform {
        private final int hOffset;
        public SuspensionStrengthValueBox(int hOffset) { this.hOffset = hOffset; }
        @Override public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
            Direction facing = state.getValue(CopycatWheelMountIIBlock.HORIZONTAL_FACING);
            TransformStack.of(ms).rotateYDegrees(AngleHelper.horizontalAngle(facing) + 180).rotateXDegrees(90);
        }
        @Override public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            Direction facing = state.getValue(CopycatWheelMountIIBlock.HORIZONTAL_FACING);
            float stateAngle = AngleHelper.horizontalAngle(facing) + 180.0F;
            return VecHelper.rotateCentered(VecHelper.voxelSpace(8 + hOffset, 15.5, 8), stateAngle, Axis.Y);
        }
    }
}