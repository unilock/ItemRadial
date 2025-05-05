package com.mosheng.itemradial.client;

import com.mosheng.itemradial.ItemRadial;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;

public class ItemRadialClient implements ClientModInitializer {
    private static KeyBinding radialMenuKey;
    private static boolean isRadialMenuOpen = false;
    private static long glfwWindowHandle;
    private static GLFWKeyCallbackI originalKeyCallback;
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
        radialMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.item-radial.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.item-radial.keys"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (glfwWindowHandle == 0 && client.getWindow() != null) {
                setupKeyCallbacks(client);
            }
        });

        ItemRadial.LOGGER.info("物品径向菜单客户端初始化完成！");
    }

    private void setupKeyCallbacks(MinecraftClient client) {
        glfwWindowHandle = client.getWindow().getHandle();
        originalKeyCallback = GLFW.glfwSetKeyCallback(glfwWindowHandle, (window, key, scancode, action, modifiers) -> {
            // 先执行原有的回调
            if (originalKeyCallback != null) {
                originalKeyCallback.invoke(window, key, scancode, action, modifiers);
            }

            // 只处理我们绑定的按键
            if (key == radialMenuKey.getDefaultKey().getCode()) {
                boolean isPressed = action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT;

                // 按键刚刚按下
                if (isPressed && !keyWasPressed) {
                    client.execute(() -> {
                        if (!isRadialMenuOpen && client.player != null) {
                            openRadialMenu(client);
                        }
                    });
                }
                // 按键刚刚释放
                else if (!isPressed && keyWasPressed) {
                    client.execute(() -> {
                        if (isRadialMenuOpen && client.currentScreen instanceof RadialMenuScreen) {
                            ((RadialMenuScreen) client.currentScreen).selectHoveredItem();
                            closeRadialMenu(client);
                        }
                    });
                }

                keyWasPressed = isPressed;
            }
        });
    }

    private void openRadialMenu(MinecraftClient client) {
        client.setScreen(new RadialMenuScreen());
        isRadialMenuOpen = true;
        // 播放翻书音效
        client.player.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
        ItemRadial.LOGGER.debug("径向菜单已打开");
    }

    private void closeRadialMenu(MinecraftClient client) {
        client.setScreen(null);
        isRadialMenuOpen = false;
        ItemRadial.LOGGER.debug("径向菜单已关闭");
    }

    public static void cleanup() {
        if (glfwWindowHandle != 0 && originalKeyCallback != null) {
            GLFW.glfwSetKeyCallback(glfwWindowHandle, originalKeyCallback);
        }
    }
}