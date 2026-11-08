package com.suting.copycatwheels.core;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CopycatConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue ROLL;
    public static final ModConfigSpec.BooleanValue HIDE_ACTION_MESSAGES;
    public static final ModConfigSpec.BooleanValue SHOW_CAMO_HINT;

    static {
        BUILDER.push("wheel");
        ROLL = BUILDER.comment("Rolling resistance coefficient")
                .defineInRange("roll", 0.1, 0.0, 100.0);
        BUILDER.pop();

        BUILDER.push("client");
        HIDE_ACTION_MESSAGES = BUILDER
                .comment("Hide other action messages shown above the hotbar")
                .define("hideActionMessages", true);

        SHOW_CAMO_HINT = BUILDER
                .comment("Default state of copycat material hint. Runtime toggle with key bind.")
                .define("showCamoHint", false);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}