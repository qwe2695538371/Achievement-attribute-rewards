// SPDX-License-Identifier: MIT
package com.playerattributemanagement;

import com.playerattributemanagement.client.ClientAttributeCache;
import com.playerattributemanagement.client.PlayerAttributeKeyBindings;
import com.playerattributemanagement.client.PlayerAttributeScreen;
import com.playerattributemanagement.common.network.AttributeSnapshot;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class AttributeachievementrewardsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PlayerAttributeKeyBindings.register();
		ClientPlayNetworking.registerGlobalReceiver(ModNetworking.SYNC_ATTRIBUTES, (client, handler, buf, responseSender) -> {
			boolean open = buf.readBoolean();
			int size = buf.readVarInt();
			List<AttributeSnapshot> entries = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				Identifier id = buf.readIdentifier();
				double extra = buf.readDouble();
				double base = buf.readDouble();
				double total = buf.readDouble();
				String customName = buf.readBoolean() ? buf.readString() : null;
				entries.add(new AttributeSnapshot(id.toString(), base, extra, total, customName));
			}

			client.execute(() -> {
				ClientAttributeCache.set(entries);
				if (open) {
					MinecraftClient.getInstance().setScreen(new PlayerAttributeScreen());
				}
			});
		});
	}
}
