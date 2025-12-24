package com.playerattributemanagement.common.attribute;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class AttributeIdAliasMap {
    private static final Map<String, String> LEGACY_PREFIXES = legacyPrefixes();

    private final Map<String, String> aliasToCanonical;

    private AttributeIdAliasMap(Map<String, String> aliasToCanonical) {
        this.aliasToCanonical = aliasToCanonical;
    }

    public static AttributeIdAliasMap fromCanonicalIds(Collection<String> canonicalIds, Map<String, String> extraAliases) {
        Map<String, String> map = new HashMap<>();
        if (canonicalIds != null) {
            for (String id : canonicalIds) {
                String canonical = normalizeInput(id);
                if (canonical == null) {
                    continue;
                }
                map.put(canonical, canonical);
                addLegacyAliases(map, canonical);
            }
        }
        if (extraAliases != null) {
            extraAliases.forEach((alias, canonical) -> {
                String normalizedAlias = normalizeInput(alias);
                String normalizedCanonical = normalizeInput(canonical);
                if (normalizedAlias != null && normalizedCanonical != null) {
                    map.put(normalizedAlias, normalizedCanonical);
                }
            });
        }
        return new AttributeIdAliasMap(Collections.unmodifiableMap(map));
    }

    public String normalize(String rawId) {
        String normalized = normalizeInput(rawId);
        if (normalized == null) {
            return null;
        }
        return aliasToCanonical.getOrDefault(normalized, normalized);
    }

    private static void addLegacyAliases(Map<String, String> map, String canonical) {
        String[] parts = splitId(canonical);
        String namespace = parts[0];
        String path = parts[1];
        int dotIndex = path.indexOf('.');
        if (dotIndex > 0) {
            String stripped = path.substring(dotIndex + 1);
            map.putIfAbsent(namespace + ":" + stripped, canonical);
            return;
        }

        String prefix = LEGACY_PREFIXES.get(path);
        if (prefix != null) {
            map.putIfAbsent(namespace + ":" + prefix + "." + path, canonical);
        }
    }

    private static String normalizeInput(String rawId) {
        if (rawId == null) {
            return null;
        }
        String trimmed = rawId.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.indexOf(':') < 0) {
            return "minecraft:" + trimmed;
        }
        return trimmed;
    }

    private static String[] splitId(String id) {
        int colon = id.indexOf(':');
        if (colon < 0) {
            return new String[] { "minecraft", id };
        }
        return new String[] { id.substring(0, colon).toLowerCase(Locale.ROOT), id.substring(colon + 1) };
    }

    private static Map<String, String> legacyPrefixes() {
        Map<String, String> map = new HashMap<>();
        add(map, "generic",
            "max_health",
            "knockback_resistance",
            "movement_speed",
            "armor",
            "armor_toughness",
            "max_absorption",
            "step_height",
            "scale",
            "gravity",
            "safe_fall_distance",
            "fall_damage_multiplier",
            "jump_strength",
            "oxygen_bonus",
            "burning_time",
            "explosion_knockback_resistance",
            "water_movement_efficiency",
            "movement_efficiency",
            "attack_knockback",
            "attack_damage",
            "attack_speed",
            "luck"
        );
        add(map, "player",
            "block_interaction_range",
            "entity_interaction_range",
            "block_break_speed",
            "submerged_mining_speed",
            "sneaking_speed",
            "mining_efficiency",
            "sweeping_damage_ratio"
        );
        return Collections.unmodifiableMap(map);
    }

    private static void add(Map<String, String> map, String prefix, String... names) {
        for (String name : names) {
            map.put(name, prefix);
        }
    }
}
