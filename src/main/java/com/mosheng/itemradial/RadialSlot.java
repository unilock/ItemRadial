package com.mosheng.itemradial;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class RadialSlot {
    private static final int SIZE = 24;
    private static final int PADDING = 4;
    private static final int BACKGROUND_OPACITY = 0x60; // 背景透明度 (0-255)
    private static final int HOVER_OPACITY = 0x30; // 悬停效果透明度

    private final int x;
    private final int y;
    private final int inventoryIndex;
    private final ItemStack stack;

    public static int getSize() {
        return SIZE;
    }

    public RadialSlot(int x, int y, int inventoryIndex, ItemStack stack) {
        this.x = x;
        this.y = y;
        this.inventoryIndex = inventoryIndex;
        this.stack = stack;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + SIZE &&
                mouseY >= y && mouseY < y + SIZE;
    }

    public void render(DrawContext context) {
        // 绘制平滑圆角槽位背景
        drawSlotBackground(context);

        // 绘制物品
        if (!stack.isEmpty()) {
            context.drawItem(stack, x + PADDING, y + PADDING);
            context.drawItemInSlot(MinecraftClient.getInstance().textRenderer, stack, x + PADDING, y + PADDING);
        }
    }

    public void renderHoverEffect(DrawContext context) {
        ItemRadial.Theme theme = ItemRadial.getCurrentTheme();
        int hoverColor = switch (theme) {
			case VANILLA -> 0x60FFFFFF; // 原版风格的白色悬停
			case MINIMAL -> 0x40FFFFFF; // 极简风格的轻微悬停
			default -> (HOVER_OPACITY << 24) | 0xFFFFFF;
		};

        context.fill(x, y, x + SIZE, y + SIZE, hoverColor);

        if (theme == ItemRadial.Theme.COLORFUL) {
            int glowColor = (0x20 << 24) | 0xFFFFFF;
            context.fill(x - 1, y - 1, x + SIZE + 1, y + SIZE + 1, glowColor);
        }
    }

    private void drawSlotBackground(DrawContext context) {
        int bgColor = getBackgroundColor();

        // 根据主题决定是否绘制圆角
        if (ItemRadial.getCurrentTheme() != ItemRadial.Theme.MINIMAL) {
            int radius = 4;
            context.fill(x + radius, y, x + SIZE - radius, y + SIZE, bgColor);
            context.fill(x, y + radius, x + SIZE, y + SIZE - radius, bgColor);
            fillCircleQuadrant(context, x + radius, y + radius, radius, bgColor, 0);
            fillCircleQuadrant(context, x + SIZE - radius, y + radius, radius, bgColor, 1);
            fillCircleQuadrant(context, x + SIZE - radius, y + SIZE - radius, radius, bgColor, 2);
            fillCircleQuadrant(context, x + radius, y + SIZE - radius, radius, bgColor, 3);
        } else {
            // 极简风格只绘制简单矩形
            context.fill(x, y, x + SIZE, y + SIZE, bgColor);
        }
    }

    private int getBackgroundColor() {
        ItemRadial.Theme theme = ItemRadial.getCurrentTheme();

        switch (theme) {
            case VANILLA:
                return 0x60C0C0C0; // 原版风格的灰色背景
            case MINIMAL:
                return 0x20000000; // 极简风格的半透明黑色
            case COLORFUL:
            default:
                int baseColor = calculateGroupColor();
                int r = (baseColor >> 16) & 0xFF;
                int g = (baseColor >> 8) & 0xFF;
                int b = baseColor & 0xFF;
                return (BACKGROUND_OPACITY << 24) | (r << 16) | (g << 8) | b;
        }
    }


    private void fillCircleQuadrant(DrawContext context, int centerX, int centerY, int radius, int color, int quadrant) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x*x + y*y <= radius*radius) {
                    // 检查象限
                    boolean inQuadrant = switch (quadrant) {
						case 0 -> (x <= 0 && y <= 0); // 左上
						case 1 -> (x >= 0 && y <= 0); // 右上
						case 2 -> (x >= 0 && y >= 0); // 右下
						case 3 -> (x <= 0 && y >= 0); // 左下
						default -> false; 
					};

                    if (inQuadrant) {
                        context.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
                    }
                }
            }
        }
    }

    private int calculateGroupColor() {
        // 使用更柔和的颜色
        if (inventoryIndex < 6) return 0x55CCFF;  // 浅蓝色 - 快捷栏
        if (inventoryIndex < 18) return 0xFFCC55; // 浅橙色 - 主物品栏
        return 0x88FF88;                          // 浅绿色 - 盔甲栏
    }

    public void onClick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null) return;

        PlayerInventory inventory = client.player.getInventory();
        if (inventoryIndex < 0 || inventoryIndex >= inventory.size()) return;

        if (inventoryIndex < 9) {
//            client.interactionManager.pickFromInventory(inventoryIndex);
            inventory.selectedSlot = inventoryIndex;
        } else {
            int hotbarSlot = inventory.selectedSlot;
            client.interactionManager.clickSlot(
                    client.player.playerScreenHandler.syncId,
                    inventoryIndex,
                    hotbarSlot,
                    SlotActionType.SWAP,
                    client.player
            );
        }
    }

    public ItemStack getStack() {
        return stack;
    }
}