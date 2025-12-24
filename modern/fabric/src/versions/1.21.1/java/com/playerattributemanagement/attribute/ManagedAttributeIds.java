package com.playerattributemanagement.attribute;

import com.playerattributemanagement.config.AttributePanelConfig;
import com.playerattributemanagement.util.Identifiers;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.util.Identifier;

public final class ManagedAttributeIds {
	private static final Set<Identifier> BASE = new HashSet<>();
	private static final Set<Identifier> MANAGED = new HashSet<>();

	static {
		// generic.*
		BASE.add(Identifiers.fromPath("generic.max_health"));
		BASE.add(Identifiers.fromPath("generic.follow_range"));
		BASE.add(Identifiers.fromPath("generic.knockback_resistance"));
		BASE.add(Identifiers.fromPath("generic.movement_speed"));
		BASE.add(Identifiers.fromPath("generic.flying_speed"));
		BASE.add(Identifiers.fromPath("generic.attack_damage"));
		BASE.add(Identifiers.fromPath("generic.attack_knockback"));
		BASE.add(Identifiers.fromPath("generic.attack_speed"));
		BASE.add(Identifiers.fromPath("generic.armor"));
		BASE.add(Identifiers.fromPath("generic.armor_toughness"));
		BASE.add(Identifiers.fromPath("generic.luck"));
		BASE.add(Identifiers.fromPath("generic.max_absorption"));
		BASE.add(Identifiers.fromPath("generic.burning_time"));
		BASE.add(Identifiers.fromPath("generic.explosion_knockback_resistance"));
		BASE.add(Identifiers.fromPath("generic.fall_damage_multiplier"));
		BASE.add(Identifiers.fromPath("generic.gravity"));
		BASE.add(Identifiers.fromPath("generic.jump_strength"));
		BASE.add(Identifiers.fromPath("generic.movement_efficiency"));
		BASE.add(Identifiers.fromPath("generic.oxygen_bonus"));
		BASE.add(Identifiers.fromPath("generic.safe_fall_distance"));
		BASE.add(Identifiers.fromPath("generic.scale"));
		BASE.add(Identifiers.fromPath("generic.step_height"));
		BASE.add(Identifiers.fromPath("generic.water_movement_efficiency"));

		// player.*
		BASE.add(Identifiers.fromPath("player.block_break_speed"));
		BASE.add(Identifiers.fromPath("player.block_interaction_range"));
		BASE.add(Identifiers.fromPath("player.entity_interaction_range"));
		BASE.add(Identifiers.fromPath("player.mining_efficiency"));
		BASE.add(Identifiers.fromPath("player.sneaking_speed"));
		BASE.add(Identifiers.fromPath("player.submerged_mining_speed"));
		BASE.add(Identifiers.fromPath("player.sweeping_damage_ratio"));

		// legacy attributes still referenced elsewhere
		BASE.add(Identifiers.fromPath("zombie.spawn_reinforcements"));
		BASE.add(Identifiers.fromPath("horse.jump_strength"));
	}

	private ManagedAttributeIds() {
	}

	public static void reload(AttributePanelConfig panelConfig) {
		MANAGED.clear();
		MANAGED.addAll(BASE);
		MANAGED.addAll(panelConfig.allManagedIds());
	}

	public static Set<Identifier> all() {
		return Collections.unmodifiableSet(MANAGED);
	}

	public static Set<Identifier> baseIds() {
		return Collections.unmodifiableSet(BASE);
	}
}
