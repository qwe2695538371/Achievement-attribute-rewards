package com.playerattributemanagement.attribute;

import com.playerattributemanagement.config.AttributePanelConfig;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;

/**
 * 维护 Mod 关注管理的一组属性，限制为 Forge 1.19.2 真实可用的 ID，同时支持配置托管的自定义属性。
 */
public final class PlayerAttributeKeys {
    private static final List<ResourceLocation> BASE_MANAGED = List.of(
        rl("generic.max_health"),
        rl("generic.follow_range"),
        rl("generic.knockback_resistance"),
        rl("generic.movement_speed"),
        rl("generic.flying_speed"),
        rl("generic.attack_damage"),
        rl("generic.attack_knockback"),
        rl("generic.attack_speed"),
        rl("generic.armor"),
        rl("generic.armor_toughness"),
        rl("generic.luck"),
        rl("zombie.spawn_reinforcements"),
        rl("horse.jump_strength"),
        rl("forge:swim_speed"),
        rl("forge:nametag_distance"),
        rl("forge:entity_gravity"),
        rl("forge:reach_distance"),
        rl("forge:attack_range"),
        rl("forge:step_height_addition")
    );

    private static final Set<ResourceLocation> BASE_MANAGED_IDS = Collections.unmodifiableSet(new LinkedHashSet<>(BASE_MANAGED));

    private PlayerAttributeKeys() {}

    public static Stream<ResourceLocation> baseManagedIds() {
        return BASE_MANAGED_IDS.stream();
    }

    public static Stream<ResourceLocation> managedIds() {
        return AttributePanelConfig.get().managedIdStream();
    }

    public static boolean isManaged(ResourceLocation id) {
        return id != null && managedIdSet().contains(id);
    }

    public static Set<ResourceLocation> managedIdSet() {
        return managedIds().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Stream<ResourceLocation> visibleAttributeIds() {
        return AttributePanelConfig.get().visibleAttributeIds();
    }

    private static ResourceLocation rl(String id) {
        return Objects.requireNonNull(ResourceLocation.tryParse(id), () -> "Invalid attribute id: " + id);
    }
}

