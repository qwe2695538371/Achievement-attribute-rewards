package com.playerattributemanagement;

import com.playerattributemanagement.attribute.PlayerAttributeApplier;
import com.playerattributemanagement.attribute.PlayerAttributeStore;
import com.playerattributemanagement.common.network.AttributeSnapshot;
import com.playerattributemanagement.config.AttributePanelConfig;
import com.playerattributemanagement.util.Identifiers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class ModNetworking {
	public static final Identifier SYNC_ATTRIBUTES = new Identifier(Attributeachievementrewards.MOD_ID, "sync_attributes");
	public static final Identifier REQUEST_OPEN_PANEL = new Identifier(Attributeachievementrewards.MOD_ID, "request_open_panel");

	private ModNetworking() {
	}

	public static void registerServerReceivers() {
		ServerPlayNetworking.registerGlobalReceiver(REQUEST_OPEN_PANEL, (server, player, handler, buf, responseSender) -> server.execute(() -> {
			if (!AttributePanelConfig.get().isPanelEnabled()) {
				player.sendMessage(net.minecraft.text.Text.translatable("message.playerattributemanagement.panel_disabled"), false);
				return;
			}
			sendSnapshot(player, true);
		}));
	}

	public static void sendSnapshot(ServerPlayerEntity player, boolean openScreen) {
		Map<Identifier, Double> extras = PlayerAttributeStore.getExtras(player);
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(openScreen);

		AttributePanelConfig panelConfig = AttributePanelConfig.get();
		List<Identifier> ordered = panelConfig.visibleManagedIds();
		if (ordered.isEmpty()) {
			ordered = new ArrayList<>(extras.keySet());
			ordered.sort(Identifier::compareTo);
		}

		List<AttributeSnapshot> snapshots = new ArrayList<>();
		for (Identifier id : ordered) {
			double extra = extras.getOrDefault(id, 0.0);
			EntityAttributeInstance instance = PlayerAttributeApplier.getAttributeInstance(player, id);
			double base = instance != null ? instance.getBaseValue() : 0.0;
			double total = instance != null ? instance.getValue() : extra;
			String customName = panelConfig.getCustomDisplayName(id);
			snapshots.add(new AttributeSnapshot(id.toString(), base, extra, total, customName));
		}

		buf.writeVarInt(snapshots.size());
		for (AttributeSnapshot snapshot : snapshots) {
			Identifier id = Identifier.tryParse(snapshot.id());
			if (id == null) {
				id = Identifiers.fromNamespacePath("minecraft", snapshot.id());
			}
			buf.writeIdentifier(id);
			buf.writeDouble(snapshot.extraValue());
			buf.writeDouble(snapshot.baseValue());
			buf.writeDouble(snapshot.totalValue());
			String customName = snapshot.customName();
			buf.writeBoolean(customName != null && !customName.isBlank());
			if (customName != null && !customName.isBlank()) {
				buf.writeString(customName);
			}
		}

		ServerPlayNetworking.send(player, SYNC_ATTRIBUTES, buf);
	}
}
