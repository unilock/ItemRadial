package com.mosheng.itemradial.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.List;

public class RadialMenuScreen extends Screen {
    private static final Text TITLE = Text.translatable("item-radial.menu.title");
    private static final int ITEM_SIZE = 24;
    private static final int PREVIEW_SIZE = 48; // 放大后的预览大小
    private static final float PREVIEW_SCALE = 2.0f; // 放大倍数

    private final RadialLayout layout;
    private List<RadialSlot> slots;
    private RadialSlot hoveredSlot;
    private long openTime;

    public RadialMenuScreen() {// TODO: 构造函数
        super(TITLE);
        this.layout = new RadialLayout();
        this.openTime = System.currentTimeMillis();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {// TODO: 绘制菜单界面
        super.render(context, mouseX, mouseY, delta);
        this.renderBackground(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 更新布局和槽位
        PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();
        this.slots = layout.generateSlots(centerX, centerY, inventory);

        // 更新悬停的格子
        updateHoveredSlot(mouseX, mouseY);

        // 绘制径向菜单
        drawRadialMenu(context);

        // 绘制中心预览（无背景）
        drawCenterPreview(context, centerX, centerY);

        // 绘制悬停提示
        drawHoverTooltip(context, mouseX, mouseY);

        // 绘制主题切换按钮
        drawThemeButton(context, mouseX, mouseY);
    }

    private void drawThemeButton(DrawContext context, int mouseX, int mouseY) {
        int buttonX = 10;
        int buttonY = 10;
        int buttonWidth = 60;
        int buttonHeight = 15;

        // 按钮背景
        int bgColor = 0x80000000;
        context.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, bgColor);

        // 按钮边框
        int borderColor = isMouseOverThemeButton(mouseX, mouseY) ? 0xFFDDDDDD : 0xFF888888;
        context.drawBorder(buttonX, buttonY, buttonWidth, buttonHeight, borderColor);

        // 按钮文本
        Text buttonText = Text.translatable("item-radial.theme." + ItemRadialClient.getCurrentTheme().name().toLowerCase());
        context.drawCenteredTextWithShadow(textRenderer, buttonText,
                buttonX + buttonWidth / 2, buttonY + (buttonHeight - 8) / 2, 0xFFFFFF);
    }

    private boolean isMouseOverThemeButton(int mouseX, int mouseY) {
        int buttonX = 10;
        int buttonY = 10;
        int buttonWidth = 80;
        int buttonHeight = 20;
        return mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
                mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {// TODO: 处理鼠标点击
        if (button == 0 && isMouseOverThemeButton((int)mouseX, (int)mouseY)) {
            ItemRadialClient.cycleTheme();
            MinecraftClient.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void updateHoveredSlot(int mouseX, int mouseY) {// TODO: 更新悬停的格子
        hoveredSlot = null;
        for (RadialSlot slot : slots) {
            if (slot.isMouseOver(mouseX, mouseY)) {
                hoveredSlot = slot;
                break;
            }
        }
    }

    private void drawRadialMenu(DrawContext context) {// TODO: 绘制径向菜单
        for (RadialSlot slot : slots) {
            slot.render(context);
        }
    }


    private void drawCenterPreview(DrawContext context, int centerX, int centerY) {// TODO: 绘制中心预览
        if (hoveredSlot != null && !hoveredSlot.getStack().isEmpty()) {
            // 保存当前变换状态
            context.getMatrices().push();

            // 计算物品原始大小居中位置
            int originalX = centerX - 8;  // 物品默认宽度16/2=8
            int originalY = centerY - 8;  // 物品默认高度16/2=8

            // 应用放大变换（以物品中心点为基准）
            context.getMatrices().translate(centerX, centerY, 0);
            context.getMatrices().scale(PREVIEW_SCALE, PREVIEW_SCALE, 1);
            context.getMatrices().translate(-8, -8, 0); // 回移半个物品大小

            // 绘制放大物品
            context.drawItem(hoveredSlot.getStack(), 0, 0);
            context.drawItemInSlot(
                    MinecraftClient.getInstance().textRenderer,
                    hoveredSlot.getStack(),
                    0,
                    0
            );

            // 恢复变换状态
            context.getMatrices().pop();
        }
    }

    private void drawHoverTooltip(DrawContext context, int mouseX, int mouseY) {// TODO: 实现悬停提示
        if (hoveredSlot != null) {
            hoveredSlot.renderHoverEffect(context);
            if (!hoveredSlot.getStack().isEmpty()) {
                context.drawItemTooltip(textRenderer, hoveredSlot.getStack(), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean shouldPause() {// TODO: 不暂停游戏
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {// TODO: 按ESC关闭菜单
        return true;
    }

    public void selectHoveredItem() {// TODO: 选中悬停物品
        if (hoveredSlot != null) {
            hoveredSlot.onClick();
        }
        this.close();
    }
}