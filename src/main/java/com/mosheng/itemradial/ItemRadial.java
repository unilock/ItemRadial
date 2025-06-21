package com.mosheng.itemradial;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemRadial implements ClientModInitializer {
    public static final String MOD_ID = "itemradial";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final KeyBinding radialMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.itemradial.open_menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.itemradial.keys"
    ));
    private static boolean keyWasPressed = false;
    public enum Theme {
        COLORFUL,  // 炫丽风格
        VANILLA,   // 原版风格
        MINIMAL    // 极简风格
    }
    private static Theme currentTheme = Theme.COLORFUL;

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    public static void cycleTheme() {
        currentTheme = Theme.values()[(currentTheme.ordinal() + 1) % Theme.values().length];
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_WORLD_TICK.register(c -> {
            MinecraftClient client = MinecraftClient.getInstance();

            // 只处理我们绑定的按键
            boolean isPressed = InputUtil.isKeyPressed(client.getWindow().getHandle(), KeyBindingHelper.getBoundKeyOf(radialMenuKey).getCode());

            // 按键刚刚按下
            if (isPressed && !keyWasPressed) {
                client.execute(() -> {
                    if (MinecraftClient.getInstance().currentScreen == null) {
                        MinecraftClient.getInstance().setScreen(new RadialMenuScreen());
                    }
                });
            }

            // 按键刚刚释放
            if (!isPressed && keyWasPressed) {
                client.execute(() -> {
                    if (MinecraftClient.getInstance().currentScreen instanceof RadialMenuScreen radialMenuScreen) {
                        radialMenuScreen.selectHoveredItem();
                        radialMenuScreen.close();
                    }
                });
            }

            keyWasPressed = isPressed;
        });
    }
}