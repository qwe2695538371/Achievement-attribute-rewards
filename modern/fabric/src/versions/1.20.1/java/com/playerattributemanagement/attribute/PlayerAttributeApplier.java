package com.playerattributemanagement.attribute;

import com.playerattributemanagement.Attributeachievementrewards;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class PlayerAttributeApplier {
	private static final String MODIFIER_NAME = Attributeachievementrewards.MOD_ID + ":extra";

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

			UUID uuid = modifierUuid(id);
			instance.removeModifier(uuid);
			double extra = extras.getOrDefault(id, 0.0);
			if (extra != 0.0) {
				EntityAttributeModifier modifier = new EntityAttributeModifier(uuid, MODIFIER_NAME, extra, EntityAttributeModifier.Operation.ADDITION);
				instance.addPersistentModifier(modifier);
			}
		}
	}

	public static EntityAttributeInstance getAttributeInstance(ServerPlayerEntity player, Identifier id) {
		EntityAttribute attribute = Registries.ATTRIBUTE.getOrEmpty(id).orElse(null);
		if (attribute == null || attribute == EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS) {
			return null;
		}
		return player.getAttributeInstance(attribute);
	}

	private static UUID modifierUuid(Identifier id) {
		return UUID.nameUUIDFromBytes((Attributeachievementrewards.MOD_ID + ":" + id.toString()).getBytes(StandardCharsets.UTF_8));
	}
}
