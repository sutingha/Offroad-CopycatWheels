package com.suting.copycatwheels;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CopycatConfig {

    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec.DoubleValue ROLL;

    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec.BooleanValue HIDE_ACTION_MESSAGES;
    public static final ModConfigSpec.BooleanValue SHOW_CAMO_HINT;

    static {
        final ModConfigSpec.Builder serverBuilder = new ModConfigSpec.Builder();
        serverBuilder.push("wheel");
        serverBuilder.comment("Copycat wheel configuration");

        ROLL = serverBuilder
                .comment("Rolling resistance coefficient")
                .defineInRange("roll", 0.1, 0.0, 100.0);

        serverBuilder.pop();
        SERVER_SPEC = serverBuilder.build();

        final ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
        clientBuilder.push("client");
        clientBuilder.comment("Copycat wheel client configuration");

        HIDE_ACTION_MESSAGES = clientBuilder
                .comment("Hide action messages shown above the hotbar when manipulating copycat wheels")
                .define("hideActionMessages", true);

        SHOW_CAMO_HINT = clientBuilder
                .comment("Show material hint when applying copycat block. Can be toggled in-game via key bind.")
                .define("showCamoHint", false);

        clientBuilder.pop();
        CLIENT_SPEC = clientBuilder.build();
    }
}