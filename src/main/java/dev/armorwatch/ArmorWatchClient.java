package dev.armorwatch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Клиентский мод (1.21.11): пока ты смотришь на игрока/моба, справа от прицела
 * рисуется панель с прочностью каждой части его брони.
 */
public class ArmorWatchClient implements ClientModInitializer {
    public static final String MOD_ID = "armorwatch";

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private static final int ROW_HEIGHT = 18;
    private static final int PANEL_WIDTH = 124;

    @Override
    public void onInitializeClient() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "target_armor"),
                ArmorWatchClient::render
        );
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Entity target = mc.crosshairPickEntity;
        if (!(target instanceof LivingEntity living) || living == mc.player) return;

        boolean hasArmor = false;
        for (EquipmentSlot slot : SLOTS) {
            if (!living.getItemBySlot(slot).isEmpty()) {
                hasArmor = true;
                break;
            }
        }
        if (!hasArmor) return;

        Font font = mc.font;
        int panelHeight = 14 + SLOTS.length * ROW_HEIGHT;
        int x = graphics.guiWidth() / 2 + 24;
        int y = graphics.guiHeight() / 2 - panelHeight / 2;

        graphics.fill(x - 4, y - 4, x + PANEL_WIDTH, y + panelHeight, 0x90000000);
        graphics.drawString(font, living.getName(), x, y, 0xFFFFFFFF, true);

        int rowY = y + 14;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = living.getItemBySlot(slot);

            if (stack.isEmpty()) {
                graphics.drawString(font, "-", x + 20, rowY + 4, 0xFF666666, true);
            } else {
                graphics.renderItem(stack, x, rowY);

                String text;
                int color;
                if (stack.isDamageableItem()) {
                    int max = stack.getMaxDamage();
                    int left = max - stack.getDamageValue();
                    float fraction = Mth.clamp((float) left / (float) max, 0.0F, 1.0F);
                    text = left + "/" + max + " (" + Math.round(fraction * 100.0F) + "%)";
                    color = 0xFF000000 | Mth.hsvToRgb(fraction / 3.0F, 1.0F, 1.0F);
                } else {
                    text = "unbreakable";
                    color = 0xFFAAAAAA;
                }
                graphics.drawString(font, text, x + 20, rowY + 4, color, true);
            }
            rowY += ROW_HEIGHT;
        }
    }
}
