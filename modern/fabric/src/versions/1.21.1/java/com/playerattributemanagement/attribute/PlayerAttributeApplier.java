package com.playerattributemanagement.attribute;

import com.playerattributemanagement.Attributeachievementrewards;
import com.playerattributemanagement.util.Identifiers;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class PlayerAttributeApplier {
	private PlayerAttributeApplier() {
	}

	public static void apply(ServerPlayerEntity player, Map<Identifier, Double> extras) {
		Set<Identifier> managed = new HashSet<>(ManagedAttributeIds.all());
		managed.addAll(extras.keySet());

		for (Identifier id : managed) {
			EntityAttributeInstance instance = getAttributeInstance(player, id);
			if (instance == null) {
				continue;
			}

			Identifier modifierId = modifierId(id);
			instance.removeModifier(modifierId);
			double extra = extras.getOrDefault(id, 0.0);
			if (extra != 0.0) {
				EntityAttributeModifier modifier = new EntityAttributeModifier(modifierId, extra, EntityAttributeModifier.Operation.ADD_VALUE);
				instance.addPersistentModifier(modifier);
			}
		}
	}

	public static EntityAttributeInstance getAttributeInstance(ServerPlayerEntity player, Identifier id) {
		RegistryEntry<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(id).orElse(null);
		if (entry == null) {
			return null;
		}
		boolean blocked = entry.getKey().map(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS::equals).orElse(false);
		if (blocked) {
			return null;
		}
		return player.getAttributeInstance(entry);
	}

	private static Identifier modifierId(Identifier attributeId) {
		return Identifiers.namespaced(Attributeachievementrewards.MOD_ID, attributeId, "extra");
	}
}
