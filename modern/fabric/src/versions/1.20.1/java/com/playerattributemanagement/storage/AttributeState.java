package com.playerattributemanagement.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public class AttributeState extends PersistentState {
	private static final String KEY = "playerattributemanagement";
	private final Map<UUID, Map<Identifier, Double>> playerExtras = new HashMap<>();

	public static AttributeState get(ServerWorld world) {
		PersistentStateManager manager = world.getPersistentStateManager();
		return manager.getOrCreate(AttributeState::fromNbt, AttributeState::new, KEY);
	}

	public Map<Identifier, Double> getExtras(UUID playerId) {
		return playerExtras.computeIfAbsent(playerId, id -> new HashMap<>());
	}

	public void setExtras(UUID playerId, Map<Identifier, Double> extras) {
		playerExtras.put(playerId, new HashMap<>(extras));
		markDirty();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		NbtCompound all = new NbtCompound();
		for (Map.Entry<UUID, Map<Identifier, Double>> entry : playerExtras.entrySet()) {
			NbtCompound values = new NbtCompound();
			for (Map.Entry<Identifier, Double> valueEntry : entry.getValue().entrySet()) {
				values.putDouble(valueEntry.getKey().toString(), valueEntry.getValue());
			}
			all.put(entry.getKey().toString(), values);
		}
		nbt.put("players", all);
		return nbt;
	}

	public static AttributeState fromNbt(NbtCompound nbt) {
		AttributeState state = new AttributeState();
		if (!nbt.contains("players", NbtElement.COMPOUND_TYPE)) {
			return state;
		}

		NbtCompound all = nbt.getCompound("players");
		for (String playerKey : all.getKeys()) {
			try {
				UUID id = UUID.fromString(playerKey);
				NbtCompound values = all.getCompound(playerKey);
				Map<Identifier, Double> extras = new HashMap<>();
				for (String attrId : values.getKeys()) {
					try {
						extras.put(new Identifier(attrId), values.getDouble(attrId));
					} catch (Exception ignored) {
					}
				}
				state.playerExtras.put(id, extras);
			} catch (Exception ignored) {
			}
		}
		return state;
	}
}
