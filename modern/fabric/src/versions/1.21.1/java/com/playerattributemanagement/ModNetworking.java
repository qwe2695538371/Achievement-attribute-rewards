package com.playerattributemanagement;

import com.playerattributemanagement.attribute.PlayerAttributeApplier;
import com.playerattributemanagement.attribute.PlayerAttributeStore;
import com.playerattributemanagement.config.AttributePanelConfig;
import com.playerattributemanagement.util.Identifiers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModNetworking {
	static {
		PayloadTypeRegistry.playS2C().register(SyncAttributesPayload.ID, SyncAttributesPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RequestOpenPanelPayload.ID, RequestOpenPanelPayload.CODEC);
	}

	private ModNetworking() {
	}

	public static void registerServerReceivers() {
		ServerPlayNetworking.registerGlobalReceiver(RequestOpenPanelPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				if (!AttributePanelConfig.get().isPanelEnabled()) {
					player.sendMessage(Text.translatable("message.playerattributemanagement.panel_disabled"), false);
					return;
				}
				sendSnapshot(player, true);
			});
		});
	}

	public static void sendSnapshot(ServerPlayerEntity player, boolean openScreen) {
		Map<Identifier, Double> extras = PlayerAttributeStore.getExtras(player);
		AttributePanelConfig panelConfig = AttributePanelConfig.get();
		List<Identifier> ordered = panelConfig.visibleManagedIds();
		List<SyncAttributesPayload.Entry> entries;
		if (ordered.isEmpty()) {
			entries = new ArrayList<>(extras.size());
			extras.keySet().stream().sorted(Identifier::compareTo).forEach(id -> entries.add(buildEntry(player, panelConfig, extras, id)));
		} else {
			entries = new ArrayList<>(ordered.size());
			for (Identifier id : ordered) {
				entries.add(buildEntry(player, panelConfig, extras, id));
			}
		}
		ServerPlayNetworking.send(player, new SyncAttributesPayload(openScreen, entries));
	}

	private static SyncAttributesPayload.Entry buildEntry(ServerPlayerEntity player, AttributePanelConfig panelConfig, Map<Identifier, Double> extras, Identifier id) {
		double extra = extras.getOrDefault(id, 0.0);
		EntityAttributeInstance instance = PlayerAttributeApplier.getAttributeInstance(player, id);
		double base = instance != null ? instance.getBaseValue() : 0.0;
		double total = instance != null ? instance.getValue() : extra;
		String customName = panelConfig.getCustomDisplayName(id);
		return new SyncAttributesPayload.Entry(id, base, extra, total, customName);
	}

	private static <T extends CustomPayload> CustomPayload.Id<T> payloadId(String path) {
		return new CustomPayload.Id<>(Identifiers.fromNamespacePath(Attributeachievementrewards.MOD_ID, path));
	}

	public record RequestOpenPanelPayload() implements CustomPayload {
		public static final CustomPayload.Id<RequestOpenPanelPayload> ID = payloadId("request_open_panel");
		private static final PacketCodec<RegistryByteBuf, RequestOpenPanelPayload> CODEC = new PacketCodec<>() {
			@Override
			public void encode(RegistryByteBuf buf, RequestOpenPanelPayload value) {
			}

			@Override
			public RequestOpenPanelPayload decode(RegistryByteBuf buf) {
				return new RequestOpenPanelPayload();
			}
		};

		@Override
		public CustomPayload.Id<RequestOpenPanelPayload> getId() {
			return ID;
		}
	}

	public record SyncAttributesPayload(boolean openScreen, List<Entry> entries) implements CustomPayload {
		public static final CustomPayload.Id<SyncAttributesPayload> ID = payloadId("sync_attributes");
		private static final PacketCodec<RegistryByteBuf, SyncAttributesPayload> CODEC = new PacketCodec<>() {
			@Override
			public void encode(RegistryByteBuf buf, SyncAttributesPayload value) {
				write(buf, value);
			}

			@Override
			public SyncAttributesPayload decode(RegistryByteBuf buf) {
				return read(buf);
			}
		};

		@Override
		public CustomPayload.Id<SyncAttributesPayload> getId() {
			return ID;
		}

		private static void write(RegistryByteBuf buf, SyncAttributesPayload payload) {
			buf.writeBoolean(payload.openScreen);
			buf.writeVarInt(payload.entries.size());
			for (Entry entry : payload.entries) {
				buf.writeIdentifier(entry.id());
				buf.writeDouble(entry.extra());
				buf.writeDouble(entry.base());
				buf.writeDouble(entry.total());
				buf.writeBoolean(entry.customName() != null);
				if (entry.customName() != null) {
					buf.writeString(entry.customName());
				}
			}
		}

		private static SyncAttributesPayload read(RegistryByteBuf buf) {
			boolean open = buf.readBoolean();
			int size = buf.readVarInt();
			List<Entry> entries = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
				Identifier id = buf.readIdentifier();
				double extra = buf.readDouble();
				double base = buf.readDouble();
				double total = buf.readDouble();
				String customName = buf.readBoolean() ? buf.readString() : null;
				entries.add(new Entry(id, base, extra, total, customName));
			}
			return new SyncAttributesPayload(open, entries);
		}

		public record Entry(Identifier id, double base, double extra, double total, String customName) {
		}
	}
}
