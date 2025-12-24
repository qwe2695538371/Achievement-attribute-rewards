package com.playerattributemanagement;

import com.playerattributemanagement.attribute.PlayerAttributeApplier;
import com.playerattributemanagement.attribute.PlayerAttributeStore;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ServerHooks {
	private ServerHooks() {
	}

	public static void register() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			PlayerAttributeApplier.apply(player, PlayerAttributeStore.getExtras(player));
			ModNetworking.sendSnapshot(player, false);
		});

		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			PlayerAttributeApplier.apply(newPlayer, PlayerAttributeStore.getExtras(newPlayer));
			ModNetworking.sendSnapshot(newPlayer, false);
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			server.getPlayerManager().getPlayerList().forEach(player -> {
				PlayerAttributeApplier.apply(player, PlayerAttributeStore.getExtras(player));
				ModNetworking.sendSnapshot(player, false);
			});
		});
	}
}
