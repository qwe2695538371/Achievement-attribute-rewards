// SPDX-License-Identifier: MIT
package com.playerattributemanagement;

import com.playerattributemanagement.client.ClientAttributeCache;
import com.playerattributemanagement.client.PlayerAttributeKeyBindings;
import com.playerattributemanagement.client.PlayerAttributeScreen;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class AttributeachievementrewardsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PlayerAttributeKeyBindings.register();
		ClientPlayNetworking.registerGlobalReceiver(ModNetworking.SyncAttributesPayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				List<ClientAttributeCache.Entry> entries = payload.entries().stream()
					.map(entry -> new ClientAttributeCache.Entry(entry.id(), entry.base(), entry.extra(), entry.total(), entry.customName()))
					.toList();
				ClientAttributeCache.set(entries);
				if (payload.openScreen()) {
					MinecraftClient.getInstance().setScreen(new PlayerAttributeScreen());
				}
			});
		});
	}
}
