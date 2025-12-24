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
		BASE.add(Identifiers.fromPath("generic.max_health"));
		BASE.add(Identifiers.fromPath("generic.knockback_resistance"));
		BASE.add(Identifiers.fromPath("generic.movement_speed"));
		BASE.add(Identifiers.fromPath("generic.attack_damage"));
		BASE.add(Identifiers.fromPath("generic.attack_speed"));
		BASE.add(Identifiers.fromPath("generic.armor"));
		BASE.add(Identifiers.fromPath("generic.armor_toughness"));
		BASE.add(Identifiers.fromPath("generic.luck"));
		BASE.add(Identifiers.fromPath("generic.attack_knockback"));
		BASE.add(Identifiers.fromPath("generic.follow_range"));
		BASE.add(Identifiers.fromPath("generic.flying_speed"));
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
