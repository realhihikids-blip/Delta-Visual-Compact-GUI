package dev.deltavisual;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;

public final class DeltaModules {
    private static long tick;
    private static boolean pearlWasDown, totemWasDown, elytraWasDown;

    private DeltaModules() {}

    public static void tick(MinecraftClient client) {
        tick++;
        if (client.player == null || client.currentScreen != null) return;

        boolean pearlDown = keyDown(client, DeltaConfig.pearlKey);
        boolean totemDown = keyDown(client, DeltaConfig.totemKey);
        boolean elytraDown = keyDown(client, DeltaConfig.elytraKey);
        if (DeltaConfig.pearlShortcut && pearlDown && !pearlWasDown) {
            int slot = findHotbar(client, Items.ENDER_PEARL);
            if (slot >= 0) {
                client.player.getInventory().setSelectedSlot(slot);
                DeltaVisualClient.notify("Эндер-жемчуг выбран.");
            } else DeltaVisualClient.notify("Эндер-жемчуга нет в хотбаре.");
        }
        if (DeltaConfig.totemShortcut && totemDown && !totemWasDown) {
            int slot = findHotbar(client, Items.TOTEM_OF_UNDYING);
            if (slot >= 0) {
                client.player.getInventory().setSelectedSlot(slot);
                DeltaVisualClient.notify("Тотем в хотбаре выбран. Автоматическая экипировка не выполняется.");
            } else DeltaVisualClient.notify("Тотема нет в хотбаре.");
        }
        if (DeltaConfig.elytraShortcut && elytraDown && !elytraWasDown) {
            DeltaVisualClient.notify("Elytra helper: открой инвентарь и замени предмет вручную.");
        }
        pearlWasDown = pearlDown;
        totemWasDown = totemDown;
        elytraWasDown = elytraDown;
    }

    private static boolean keyDown(MinecraftClient client, int key) {
        return key >= 0 && org.lwjgl.glfw.GLFW.glfwGetKey(client.getWindow().getHandle(), key) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
    }

    private static int findHotbar(MinecraftClient client, net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }

    public static void renderHud(DrawContext ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!DeltaConfig.showHud || client.player == null || client.options.hudHidden) return;
        int x = Math.max(0, Math.min(DeltaConfig.hudX, client.getWindow().getScaledWidth() - 140));
        int y = Math.max(0, Math.min(DeltaConfig.hudY, client.getWindow().getScaledHeight() - 90));
        int color = DeltaConfig.accentColor;
        int rows = 2;
        if (DeltaConfig.showCoordinates) rows++;
        if (DeltaConfig.showArmorHud) rows++;
        if (DeltaConfig.showClock) rows++;
        int height = 10 + rows * 12;
        ctx.fill(x, y, x + 148, y + height, withAlpha(0x15111F, DeltaConfig.panelOpacity));
        ctx.fill(x, y, x + 2, y + height, color);
        int ty = y + 6;
        ctx.drawText(client.textRenderer, "DELTA VISUAL", x + 8, ty, color, false);
        ty += 12;
        ctx.drawText(client.textRenderer, "Aura: " + (DeltaConfig.aura ? "ON" : "OFF"), x + 8, ty, 0xFFE5DDF2, false);
        ty += 12;
        if (DeltaConfig.showCoordinates) {
            var pos = client.player.getBlockPos();
            ctx.drawText(client.textRenderer, "XYZ: " + pos.getX() + " " + pos.getY() + " " + pos.getZ(), x + 8, ty, 0xFFBFB4D0, false);
            ty += 12;
        }
        if (DeltaConfig.showArmorHud) {
            int armor = client.player.getArmor();
            ctx.drawText(client.textRenderer, "Armor: " + armor, x + 8, ty, 0xFFBFB4D0, false);
            ty += 12;
        }
        if (DeltaConfig.showClock) {
            ctx.drawText(client.textRenderer, java.time.LocalTime.now().withNano(0).toString(), x + 8, ty, 0xFFBFB4D0, false);
            ty += 12;
        }
        if (DeltaConfig.reachDisplay && DeltaConfig.showTargetDistance
                && client.crosshairTarget instanceof EntityHitResult hit
                && hit.getEntity() instanceof PlayerEntity target && client.player.canSee(target)) {
            double distance = client.player.distanceTo(target);
            String label = String.format(java.util.Locale.ROOT, "Target: %.2f blocks", distance);
            ctx.fill(x, y + height + 4, x + client.textRenderer.getWidth(label) + 16, y + height + 22, withAlpha(0x15111F, DeltaConfig.panelOpacity));
            ctx.drawText(client.textRenderer, label, x + 8, y + height + 9, DeltaConfig.secondaryColor, false);
        }
    }

    private static int withAlpha(int rgb, float alpha) {
        int a = MathHelper.clamp((int)(alpha * 255), 0, 255);
        return (a << 24) | (rgb & 0xFFFFFF);
    }
}
