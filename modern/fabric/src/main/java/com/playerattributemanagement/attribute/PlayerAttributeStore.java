package com.playerattributemanagement.attribute;

import com.playerattributemanagement.ModNetworking;
import com.playerattributemanagement.common.reward.RewardFormat;
import com.playerattributemanagement.storage.AttributeState;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class PlayerAttributeStore {
	private PlayerAttributeStore() {
	}

	public static Map<Identifier, Double> getExtras(ServerPlayerEntity player) {
		return Collections.unmodifiableMap(state(player).getExtras(player.getUuid()));
	}

	public static double setExtra(ServerPlayerEntity player, Identifier id, double value) {
		Map<Identifier, Double> extras = mutableExtras(player);
		extras.put(id, value);
		save(player, extras);
		return value;
	}

	public static double addExtra(ServerPlayerEntity player, Identifier id, double delta) {
		Map<Identifier, Double> extras = mutableExtras(player);
		double updated = extras.getOrDefault(id, 0.0) + delta;
		extras.put(id, updated);
		save(player, extras);
		return updated;
	}

	public static boolean addExtras(ServerPlayerEntity player, Map<Identifier, Double> deltas) {
		if (deltas == null || deltas.isEmpty()) {
			return false;
		}

		Map<Identifier, Double> extras = mutableExtras(player);
		boolean changed = false;
		for (Map.Entry<Identifier, Double> entry : deltas.entrySet()) {
			double delta = entry.getValue();
			if (RewardFormat.isZero(delta)) {
				continue;
			}
			Identifier id = entry.getKey();
			double updated = extras.getOrDefault(id, 0.0) + delta;
			extras.put(id, updated);
			changed = true;
		}

		if (changed) {
			save(player, extras);
		}
		return changed;
	}

	public static void reset(ServerPlayerEntity player, Identifier id) {
		Map<Identifier, Double> extras = mutableExtras(player);
		extras.remove(id);
		save(player, extras);
	}

	public static void resetAll(ServerPlayerEntity player) {
		save(player, new HashMap<>());
	}

	private static void save(ServerPlayerEntity player, Map<Identifier, Double> extras) {
		state(player).setExtras(player.getUuid(), extras);
		PlayerAttributeApplier.apply(player, extras);
		ModNetworking.sendSnapshot(player, false);
	}

	private static AttributeState state(ServerPlayerEntity player) {
		ServerWorld world = player.getServer().getOverworld();
		return AttributeState.get(world);
	}

	private static Map<Identifier, Double> mutableExtras(ServerPlayerEntity player) {
		UUID id = player.getUuid();
		return new HashMap<>(state(player).getExtras(id));
	}
}
