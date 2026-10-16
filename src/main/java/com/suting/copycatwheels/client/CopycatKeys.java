package com.suting.copycatwheels.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class CopycatKeys {
    public static final String CATEGORY = "key.categories.copycatwheels";

    public static final KeyMapping CAMO_HINT_TOGGLE = new KeyMapping(
            "key.copycatwheels.camo_hint_toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
    );
}