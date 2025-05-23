//布局类
package com.mosheng.itemradial;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RadialLayout {
    private static final int[][] GROUPS = {
            {6, 40, 0},   // 数量, 半径, 起始索引
            {12, 70, 6},
            {18, 100, 18}
    };

    public static List<RadialSlot> generateSlots(int centerX, int centerY, PlayerInventory inventory) {
        List<RadialSlot> slots = new ArrayList<>();

        for (int[] group : GROUPS) {
            int count = group[0];
            int radius = group[1];
            int startIndex = group[2];

            for (int i = 0; i < count; i++) {
                double angle = 2 * Math.PI * i / count;
                int slotSize = RadialSlot.getSize();
                int x = centerX + (int)(Math.cos(angle) * radius - (double)slotSize / 2);
                int y = centerY + (int)(Math.sin(angle) * radius - (double)slotSize / 2);

                int inventoryIndex = startIndex + i;
                ItemStack stack = inventoryIndex < inventory.size() ?
                        inventory.getStack(inventoryIndex) : ItemStack.EMPTY;

                slots.add(new RadialSlot(x, y, inventoryIndex, stack));
            }
        }

        return slots;
    }
}