package com.playerattributemanagement.attribute;

import com.playerattributemanagement.Playerattributemanagement;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 将 {@link PlayerAttributeStore} 中记录的附加值应用到实体属性上。
 */
public final class PlayerAttributeApplier {
    private static final String EXTRA_PREFIX = "extra/";

    private PlayerAttributeApplier() {}

    public static void applyAndClean(LivingEntity entity, PlayerAttributeStore store) {
        Set<ResourceLocation> active = new HashSet<>();
        store.getAllExtras().forEach((id, value) -> {
            if (applySingle(entity, id, value)) {
                active.add(id);
            }
        });

        PlayerAttributeKeys.managedIds().forEach(id -> {
            if (!active.contains(id)) {
                removeModifier(entity, id);
            }
        });
    }

    private static boolean applySingle(LivingEntity entity, ResourceLocation id, double value) {
        Holder<Attribute> attributeHolder = resolveAttributeHolder(entity, id);
        if (attributeHolder == null) {
            Playerattributemanagement.LOGGER.warn("未知属性 {}，无法应用附加值", id);
            return false;
        }

        AttributeInstance instance = entity.getAttribute(attributeHolder);
        if (instance == null) {
            return false;
        }

        ResourceLocation modifierId = modifierId(id);
        instance.removeModifier(modifierId);
        if (Math.abs(value) < 1.0E-6) {
            return true;
        }

        AttributeModifier modifier = new AttributeModifier(modifierId, value, AttributeModifier.Operation.ADD_VALUE);
        instance.addPermanentModifier(modifier);
        return true;
    }

    private static void removeModifier(LivingEntity entity, ResourceLocation id) {
        Holder<Attribute> attributeHolder = resolveAttributeHolder(entity, id);
        if (attributeHolder == null) {
            return;
        }

        AttributeInstance instance = entity.getAttribute(attributeHolder);
        if (instance != null) {
            instance.removeModifier(modifierId(id));
        }
    }

    private static Holder<Attribute> resolveAttributeHolder(LivingEntity entity, ResourceLocation id) {
        return AttributeResolver.resolveHolder(id, entity.level().registryAccess());
    }

    private static ResourceLocation modifierId(ResourceLocation attributeId) {
        String path = attributeId.getNamespace() + "_" + attributeId.getPath();
        return ResourceLocation.fromNamespaceAndPath(Playerattributemanagement.MODID, EXTRA_PREFIX + path);
    }
}
