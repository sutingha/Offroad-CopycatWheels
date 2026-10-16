package com.suting.copycatwheels;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CopycatConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue ROLL;
    public static final ModConfigSpec.BooleanValue HIDE_ACTION_MESSAGES;
    public static final ModConfigSpec.BooleanValue SHOW_CAMO_HINT;

    static {
        BUILDER.push("wheel");
        BUILDER.comment("Copycat wheel configuration");

        ROLL = BUILDER
                .comment("Rolling resistance coefficient")
                .defineInRange("roll", 0.1, 0.0, 100.0);

        BUILDER.pop();

        BUILDER.push("client");
        HIDE_ACTION_MESSAGES = BUILDER
                .comment("Hide other action messages shown above the hotbar when manipulating copycat wheels")
                .define("hideActionMessages", true);

        SHOW_CAMO_HINT = BUILDER
                .comment("Show copycat material hint. Toggle with F3 + key (configurable in Controls).")
                .define("showCamoHint", false);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}