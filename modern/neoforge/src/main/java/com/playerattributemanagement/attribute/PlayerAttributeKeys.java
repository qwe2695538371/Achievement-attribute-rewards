package com.playerattributemanagement.attribute;

import com.playerattributemanagement.config.AttributePanelConfig;
import com.playerattributemanagement.attribute.AttributeResolver;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 维护 Mod 聚焦管理的一组属性，方便 UI 展示与指令校验。
 */
public final class PlayerAttributeKeys {
    private static final List<Holder<Attribute>> BASE_MANAGED = List.of(
        Attributes.MAX_HEALTH,
        Attributes.KNOCKBACK_RESISTANCE,
        Attributes.MOVEMENT_SPEED,
        Attributes.ARMOR,
        Attributes.ARMOR_TOUGHNESS,
        Attributes.MAX_ABSORPTION,
        Attributes.STEP_HEIGHT,
        Attributes.SCALE,
        Attributes.GRAVITY,
        Attributes.SAFE_FALL_DISTANCE,
        Attributes.FALL_DAMAGE_MULTIPLIER,
        Attributes.JUMP_STRENGTH,
        Attributes.OXYGEN_BONUS,
        Attributes.BURNING_TIME,
        Attributes.EXPLOSION_KNOCKBACK_RESISTANCE,
        Attributes.WATER_MOVEMENT_EFFICIENCY,
        Attributes.MOVEMENT_EFFICIENCY,
        Attributes.ATTACK_KNOCKBACK,
        Attributes.ATTACK_DAMAGE,
        Attributes.ATTACK_SPEED,
        Attributes.LUCK,
        Attributes.BLOCK_INTERACTION_RANGE,
        Attributes.ENTITY_INTERACTION_RANGE,
        Attributes.BLOCK_BREAK_SPEED,
        Attributes.SUBMERGED_MINING_SPEED,
        Attributes.SNEAKING_SPEED,
        Attributes.MINING_EFFICIENCY,
        Attributes.SWEEPING_DAMAGE_RATIO
    );

    private static final Set<ResourceLocation> BASE_MANAGED_IDS = BASE_MANAGED.stream()
        .map(PlayerAttributeKeys::idOf)
        .collect(Collectors.toCollection(LinkedHashSet::new));

    private PlayerAttributeKeys() {}

    public static Stream<Holder<Attribute>> managedAttributes() {
        return BASE_MANAGED.stream();
    }

    public static Stream<ResourceLocation> baseManagedIds() {
        return BASE_MANAGED_IDS.stream();
    }

    public static Stream<ResourceLocation> managedIds() {
        return AttributePanelConfig.get().managedIdStream();
    }

    public static boolean isManaged(ResourceLocation id) {
        return managedIdSet().contains(id);
    }

    public static Set<ResourceLocation> managedIdSet() {
        Set<ResourceLocation> merged = managedIds().collect(Collectors.toCollection(LinkedHashSet::new));
        return Collections.unmodifiableSet(merged);
    }

    public static Stream<ResourceLocation> visibleIds() {
        return AttributePanelConfig.get().visibleAttributeIds();
    }

    public static Stream<Holder<Attribute>> visibleAttributes(RegistryAccess access) {
        if (access == null) {
            return Stream.empty();
        }
        return AttributePanelConfig.get().visibleAttributeIds()
            .map(id -> AttributeResolver.resolveHolder(id, access))
            .filter(Objects::nonNull);
    }

    public static ResourceLocation idOf(Holder<Attribute> holder) {
        return holder.unwrapKey()
            .orElseThrow(() -> new IllegalStateException("Unregistered attribute holder"))
            .location();
    }
}
