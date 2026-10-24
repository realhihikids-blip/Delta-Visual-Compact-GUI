package dev.deltavisual;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DeltaConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("delta-visual.json");

    public static boolean animations = true;
    public static boolean showHud = true;
    public static boolean reachDisplay = true;
    public static boolean aura = false;
    public static boolean showVisiblePlayerNames = true;
    public static boolean showArmorStatus = true;
    public static boolean pearlShortcut = true;
    public static boolean totemShortcut = true;
    public static boolean elytraShortcut = true;
    public static int accentColor = 0xFFB45CFF;
    public static int secondaryColor = 0xFF55D6BE;
    public static float uiScale = 1.0f;
    public static float panelOpacity = 0.92f;
    public static float auraRadius = 1.25f;
    public static float auraSpeed = 1.0f;
    public static float textScale = 1.0f;
    public static String auraStyle = "Orbit";
    public static String fontStyle = "Minecraft";
    public static boolean showCoordinates = true;
    public static boolean showClock = false;
    public static boolean showKeystrokes = false;
    public static boolean showArmorHud = true;
    public static boolean showTargetDistance = true;
    public static boolean showAuraPreview = true;
    public static boolean worldTint = false;
    public static int hudX = 8;
    public static int hudY = 8;
    public static String activeProfile = "Default";
    public static int pearlKey = GLFW_KEYS.PEARL;
    public static int totemKey = GLFW_KEYS.TOTEM;
    public static int elytraKey = GLFW_KEYS.ELYTRA;

    private DeltaConfig() {}

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(new Snapshot()));
        } catch (IOException e) {
            DeltaVisualClient.notify("Не удалось сохранить настройки.");
        }
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }
        try {
            Snapshot s = GSON.fromJson(Files.readString(FILE), Snapshot.class);
            if (s == null) throw new IllegalStateException("empty config");
            animations = s.animations;
            showHud = s.showHud;
            reachDisplay = s.reachDisplay;
            aura = s.aura;
            showVisiblePlayerNames = s.showVisiblePlayerNames;
            showArmorStatus = s.showArmorStatus;
            pearlShortcut = s.pearlShortcut;
            totemShortcut = s.totemShortcut;
            elytraShortcut = s.elytraShortcut;
            accentColor = s.accentColor;
            secondaryColor = s.secondaryColor;
            uiScale = clamp(s.uiScale, 0.65f, 1.5f);
            panelOpacity = clamp(s.panelOpacity, 0.25f, 1f);
            auraRadius = clamp(s.auraRadius, 0.4f, 4f);
            auraSpeed = clamp(s.auraSpeed, 0.1f, 4f);
            textScale = clamp(s.textScale, 0.7f, 1.5f);
            auraStyle = safe(s.auraStyle, "Orbit");
            fontStyle = safe(s.fontStyle, "Minecraft");
            showCoordinates = s.showCoordinates;
            showClock = s.showClock;
            showKeystrokes = s.showKeystrokes;
            showArmorHud = s.showArmorHud;
            showTargetDistance = s.showTargetDistance;
            showAuraPreview = s.showAuraPreview;
            worldTint = s.worldTint;
            hudX = Math.max(0, s.hudX);
            hudY = Math.max(0, s.hudY);
            activeProfile = safe(s.activeProfile, "Default");
            pearlKey = s.pearlKey;
            totemKey = s.totemKey;
            elytraKey = s.elytraKey;
        } catch (Exception e) {
            try { Files.move(FILE, FILE.resolveSibling("delta-visual.json.broken"), java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
            catch (IOException ignored) {}
            DeltaVisualClient.notify("Файл настроек повреждён — загружены значения по умолчанию.");
            save();
        }
    }

    public static void reset() {
        animations = true; showHud = true; reachDisplay = true; aura = false;
        showVisiblePlayerNames = true; showArmorStatus = true;
        pearlShortcut = true; totemShortcut = true; elytraShortcut = true;
        accentColor = 0xFFB45CFF; secondaryColor = 0xFF55D6BE;
        uiScale = 1f; panelOpacity = .92f; auraRadius = 1.25f; auraSpeed = 1f;
        textScale = 1f; auraStyle = "Orbit"; fontStyle = "Minecraft";
        showCoordinates = true; showClock = false; showKeystrokes = false; showArmorHud = true;
        showTargetDistance = true; showAuraPreview = true; worldTint = false; hudX = 8; hudY = 8; activeProfile = "Default";
        pearlKey = GLFW_KEYS.PEARL; totemKey = GLFW_KEYS.TOTEM; elytraKey = GLFW_KEYS.ELYTRA;
        save();
    }

    private static float clamp(float n, float min, float max) { return Math.max(min, Math.min(max, n)); }
    private static String safe(String s, String fallback) { return s == null ? fallback : s; }

    private static final class Snapshot {
        boolean animations = DeltaConfig.animations, showHud = DeltaConfig.showHud, reachDisplay = DeltaConfig.reachDisplay;
        boolean aura = DeltaConfig.aura, showVisiblePlayerNames = DeltaConfig.showVisiblePlayerNames, showArmorStatus = DeltaConfig.showArmorStatus;
        boolean pearlShortcut = DeltaConfig.pearlShortcut, totemShortcut = DeltaConfig.totemShortcut, elytraShortcut = DeltaConfig.elytraShortcut;
        int accentColor = DeltaConfig.accentColor, secondaryColor = DeltaConfig.secondaryColor;
        float uiScale = DeltaConfig.uiScale, panelOpacity = DeltaConfig.panelOpacity, auraRadius = DeltaConfig.auraRadius, auraSpeed = DeltaConfig.auraSpeed, textScale = DeltaConfig.textScale;
        String auraStyle = DeltaConfig.auraStyle, fontStyle = DeltaConfig.fontStyle, activeProfile = DeltaConfig.activeProfile;
        boolean showCoordinates = DeltaConfig.showCoordinates, showClock = DeltaConfig.showClock, showKeystrokes = DeltaConfig.showKeystrokes;
        boolean showArmorHud = DeltaConfig.showArmorHud, showTargetDistance = DeltaConfig.showTargetDistance, showAuraPreview = DeltaConfig.showAuraPreview, worldTint = DeltaConfig.worldTint;
        int hudX = DeltaConfig.hudX, hudY = DeltaConfig.hudY;
        int pearlKey = DeltaConfig.pearlKey, totemKey = DeltaConfig.totemKey, elytraKey = DeltaConfig.elytraKey;
    }
}
