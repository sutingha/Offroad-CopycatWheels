package com.suting.copycatwheels.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CopycatWheelsMixinPlugin implements IMixinConfigPlugin {

    private static final boolean TRACKS_PRESENT = copycatWheels$classExists(
            "dev.qwxon.tracks.mixin_interface.WheelMountOffsetAccess");

    private static boolean copycatWheels$classExists(final String className) {
        try {
            Class.forName(className, false, CopycatWheelsMixinPlugin.class.getClassLoader());
            return true;
        } catch (final Throwable t) {
            return false;
        }
    }

    @Override
    public void onLoad(final String mixinPackage) {}

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        if ("com.suting.copycatwheels.mixin.CopycatWheelMountIIBlockEntityTracksMixin".equals(mixinClassName)) {
            return TRACKS_PRESENT;
        }
        return true;
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() { return null; }

    @Override
    public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {}

    @Override
    public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {}
}