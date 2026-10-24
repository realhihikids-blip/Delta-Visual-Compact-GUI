package dev.deltavisual;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class DeltaVisualClient implements ClientModInitializer {
    public static final String MOD_ID = "delta_visual";
    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        DeltaConfig.load();
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.delta_visual.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    client.setScreen(new DeltaClickGui());
                }
            }
            DeltaModules.tick(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            DeltaModules.renderHud(drawContext);
        });
    }

    public static void notify(String message) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§5[Delta Visual] §f" + message), true);
        }
    }
}
