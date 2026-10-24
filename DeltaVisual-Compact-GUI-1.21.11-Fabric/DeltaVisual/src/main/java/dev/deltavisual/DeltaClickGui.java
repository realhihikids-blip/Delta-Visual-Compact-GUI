package dev.deltavisual;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class DeltaClickGui extends Screen {
    private static final String[] CATEGORIES = {"Combat", "Movement", "Render", "Player", "Misc"};
    private static final int[] COUNTS = {5, 3, 15, 13, 6};
    private String category = "Render";
    private String selectedModule = "";
    private String waitingFor = null;
    private int panelX, panelY, panelW, panelH;
    private int scrollOffset = 0;
    private final List<UiEntry> entries = new ArrayList<>();
    private boolean draggingPanel = false;
    private double dragX, dragY;

    private record UiEntry(String category, String name, String description, boolean toggle, Consumer<Boolean> setter, java.util.function.Supplier<Boolean> getter) {}

    public DeltaClickGui() { super(Text.literal("Delta Visual")); }

    private List<UiEntry> allEntries() {
        List<UiEntry> a = new ArrayList<>();
        a.add(new UiEntry("Combat", "No Friend Damage", "Visual client safety setting", true, v -> {}, () -> false));
        a.add(new UiEntry("Movement", "Smooth Camera", "Client-side camera preference", true, v -> {}, () -> false));
        a.add(new UiEntry("Render", "Ambience", "Visual theme accent", true, v -> DeltaConfig.worldTint = v, () -> DeltaConfig.worldTint));
        a.add(new UiEntry("Render", "Animations", "GUI animation preference", true, v -> DeltaConfig.animations = v, () -> DeltaConfig.animations));
        a.add(new UiEntry("Render", "Full Bright HUD", "Bright interface only; does not change world light", true, v -> DeltaConfig.showHud = v, () -> DeltaConfig.showHud));
        a.add(new UiEntry("Render", "Entity Tags", "Use vanilla-visible player labels", true, v -> DeltaConfig.showVisiblePlayerNames = v, () -> DeltaConfig.showVisiblePlayerNames));
        a.add(new UiEntry("Render", "Reach Display", "Distance to visible player only", true, v -> DeltaConfig.reachDisplay = v, () -> DeltaConfig.reachDisplay));
        a.add(new UiEntry("Render", "Pointers", "HUD overlay", true, v -> DeltaConfig.showHud = v, () -> DeltaConfig.showHud));
        a.add(new UiEntry("Player", "Armor HUD", "Show equipped armor points", true, v -> DeltaConfig.showArmorHud = v, () -> DeltaConfig.showArmorHud));
        a.add(new UiEntry("Player", "Coordinates", "Show player coordinates", true, v -> DeltaConfig.showCoordinates = v, () -> DeltaConfig.showCoordinates));
        a.add(new UiEntry("Player", "Clock", "Show local clock in HUD", true, v -> DeltaConfig.showClock = v, () -> DeltaConfig.showClock));
        a.add(new UiEntry("Player", "Totem Swap Helper", "Manual helper; no inventory automation", true, v -> DeltaConfig.totemShortcut = v, () -> DeltaConfig.totemShortcut));
        a.add(new UiEntry("Player", "Elytra Swap Helper", "Manual helper; no hidden inventory clicks", true, v -> DeltaConfig.elytraShortcut = v, () -> DeltaConfig.elytraShortcut));
        a.add(new UiEntry("Misc", "Aura Cosmetics", "Cosmetic settings; particle renderer is not yet connected", true, v -> DeltaConfig.aura = v, () -> DeltaConfig.aura));
        a.add(new UiEntry("Misc", "Item Shortcuts", "Select pearl hotbar slot", true, v -> DeltaConfig.pearlShortcut = v, () -> DeltaConfig.pearlShortcut));
        a.add(new UiEntry("Misc", "Chat Helper", "No chat automation", false, v -> {}, () -> false));
        a.add(new UiEntry("Misc", "Configurations", "Save settings", false, v -> DeltaConfig.save(), () -> false));
        return a;
    }

    @Override
    protected void init() {
        clearChildren();
        entries.clear();
        entries.addAll(allEntries());

        panelW = Math.min(1140, width - 24);
        panelH = Math.min(500, height - 24);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        int gap = 10;
        int colW = (panelW - 2 * 18 - gap * 4) / 5;
        int colY = panelY + 38;
        for (int i = 0; i < CATEGORIES.length; i++) {
            final String cat = CATEGORIES[i];
            int x = panelX + 18 + i * (colW + gap);
            addDrawableChild(ButtonWidget.builder(Text.literal(cat + "  ·  " + COUNTS[i]), b -> {
                category = cat; selectedModule = ""; scrollOffset = 0; init();
            }).dimensions(x, colY, colW, 22).build());
        }

        int searchX = panelX + 18;
        int searchY = panelY + panelH - 34;
        addDrawableChild(ButtonWidget.builder(Text.literal("Search modules"), b -> {
            DeltaVisualClient.notify("Use category buttons to browse modules in this build.");
        }).dimensions(searchX, searchY, 150, 20).build());

        int colIndex = Math.max(0, java.util.Arrays.asList(CATEGORIES).indexOf(category));
        int x = panelX + 18 + colIndex * (colW + gap);
        int y = colY + 30;
        int bottom = panelY + panelH - 46;
        List<UiEntry> categoryEntries = entries.stream().filter(e -> e.category().equals(category)).toList();
        for (int i = scrollOffset; i < categoryEntries.size() && y + 22 <= bottom; i++) {
            UiEntry e = categoryEntries.get(i);
            int rowY = y;
            addDrawableChild(ButtonWidget.builder(Text.literal(e.name() + (e.toggle() ? (e.getter().get() ? "  ●" : "  ○") : "  ›")), b -> {
                if (e.toggle()) {
                    e.setter().accept(!e.getter().get());
                    DeltaConfig.save();
                    init();
                } else if (e.name().equals("Configurations")) {
                    DeltaConfig.save();
                    DeltaVisualClient.notify("Configuration saved.");
                } else {
                    selectedModule = e.name();
                    init();
                }
            }).dimensions(x, rowY, colW, 21).build());
            y += 24;
        }

        if (!selectedModule.isEmpty()) {
            int sx = panelX + 18 + 4 * (colW + gap);
            int sy = colY + 30;
            addDrawableChild(ButtonWidget.builder(Text.literal("‹ Back"), b -> { selectedModule = ""; init(); }).dimensions(sx, sy, colW, 20).build());
            sy += 26;
            if (selectedModule.equals("Aura Cosmetics")) {
                addDrawableChild(ButtonWidget.builder(Text.literal("Style: " + DeltaConfig.auraStyle), b -> {
                    String[] styles = {"Orbit", "Spiral", "Stars", "Pulse", "Minimal", "Dual Color", "Rainbow"};
                    int idx = java.util.Arrays.asList(styles).indexOf(DeltaConfig.auraStyle);
                    DeltaConfig.auraStyle = styles[(idx + 1) % styles.length]; saveAndInit();
                }).dimensions(sx, sy, colW, 20).build()); sy += 24;
                addDrawableChild(ButtonWidget.builder(Text.literal("Radius: " + String.format("%.2f", DeltaConfig.auraRadius)), b -> {
                    DeltaConfig.auraRadius += .25f; if (DeltaConfig.auraRadius > 3) DeltaConfig.auraRadius = .5f; saveAndInit();
                }).dimensions(sx, sy, colW, 20).build()); sy += 24;
                addDrawableChild(ButtonWidget.builder(Text.literal("Speed: " + String.format("%.1f", DeltaConfig.auraSpeed)), b -> {
                    DeltaConfig.auraSpeed += .25f; if (DeltaConfig.auraSpeed > 3) DeltaConfig.auraSpeed = .5f; saveAndInit();
                }).dimensions(sx, sy, colW, 20).build());
            } else if (selectedModule.equals("Reach Display")) {
                addDrawableChild(ButtonWidget.builder(Text.literal("Visible target only: ON"), b -> {
                    DeltaVisualClient.notify("Reach Display is informational and only reads visible targets.");
                }).dimensions(sx, sy, colW, 20).build());
            } else {
                addDrawableChild(ButtonWidget.builder(Text.literal("Reset module settings"), b -> {
                    DeltaVisualClient.notify("Use the category toggle or Configurations for this module.");
                }).dimensions(sx, sy, colW, 20).build());
            }
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
                .dimensions(panelX + panelW - 82, panelY + panelH - 32, 64, 20).build());
    }

    private void saveAndInit() { DeltaConfig.save(); init(); }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingFor != null) {
            if (keyCode == 256) { waitingFor = null; init(); return true; }
            return true;
        }
        if (keyCode == 264) { scrollOffset++; init(); return true; }
        if (keyCode == 265) { scrollOffset = Math.max(0, scrollOffset - 1); init(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset = Math.max(0, scrollOffset - (int)Math.signum(verticalAmount));
        init();
        return true;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        int alpha = Math.max(150, Math.min(245, (int)(DeltaConfig.panelOpacity * 255)));
        int panel = (alpha << 24) | 0x100D17;
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, panel);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 2, DeltaConfig.accentColor);
        ctx.drawText(textRenderer, "DELTA VISUAL", panelX + 18, panelY + 12, DeltaConfig.accentColor, false);
        ctx.drawText(textRenderer, "Visual modules", panelX + 145, panelY + 12, 0xFFC8BED4, false);

        int gap = 10;
        int colW = (panelW - 2 * 18 - gap * 4) / 5;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int x = panelX + 18 + i * (colW + gap);
            int top = panelY + 38;
            int bottom = panelY + panelH - 42;
            ctx.fill(x, top, x + colW, bottom, 0x401C1727);
            if (CATEGORIES[i].equals(category)) ctx.fill(x, top, x + colW, top + 2, DeltaConfig.accentColor);
        }

        if (!selectedModule.isEmpty()) {
            int colW2 = (panelW - 2 * 18 - 40) / 5;
            int sx = panelX + 18 + 4 * (colW2 + 10);
            ctx.fill(sx, panelY + 91, sx + colW2, panelY + panelH - 45, 0x55261B35);
            ctx.drawText(textRenderer, selectedModule, sx + 6, panelY + 82, DeltaConfig.secondaryColor, false);
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override public void close() { DeltaConfig.save(); if (client != null) client.setScreen(null); }
    @Override public boolean shouldPause() { return false; }
}

@Override
public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
    if (input.code() == 256) {
        this.close();
        return true;
    }
    return super.keyPressed(input);
}

