package com.playerattributemanagement.client;

import com.playerattributemanagement.ModNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public final class PlayerAttributeKeyBindings {
	private static KeyBinding openPanel;

	private PlayerAttributeKeyBindings() {
	}

	public static void register() {
		openPanel = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.playerattributemanagement.open_panel", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "key.categories.misc")
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null || client.world == null) {
				return;
			}
			while (openPanel.wasPressed()) {
				ClientPlayNetworking.send(ModNetworking.REQUEST_OPEN_PANEL, net.fabricmc.fabric.api.networking.v1.PacketByteBufs.empty());
			}
		});
	}
}
