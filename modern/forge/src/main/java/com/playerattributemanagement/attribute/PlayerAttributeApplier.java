package com.playerattributemanagement.attribute;

import com.playerattributemanagement.Playerattributemanagement;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * ???{@link PlayerAttributeStore} ????????????????????????????????????????????????
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
        Attribute attribute = AttributeResolver.resolveAttribute(id, null);
        if (attribute == null) {
            Playerattributemanagement.LOGGER.warn("Unknown attribute {}, cannot apply extra value", id);
            return false;
        }

        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return false;
        }

        UUID modifierId = modifierUuid(id);
        instance.removeModifier(modifierId);
        if (Math.abs(value) < 1.0E-6) {
            return true;
        }

        AttributeModifier modifier = new AttributeModifier(modifierId, modifierName(id), value, AttributeModifier.Operation.ADDITION);
        instance.addPermanentModifier(modifier);
        return true;
    }

    private static void removeModifier(LivingEntity entity, ResourceLocation id) {
        Attribute attribute = AttributeResolver.resolveAttribute(id, null);
        if (attribute == null) {
            return;
        }

        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(modifierUuid(id));
        }
    }

    private static ResourceLocation modifierId(ResourceLocation attributeId) {
        String path = attributeId.getNamespace() + "_" + attributeId.getPath();
        return new ResourceLocation(Playerattributemanagement.MODID, EXTRA_PREFIX + path);
    }

    private static UUID modifierUuid(ResourceLocation attributeId) {
        return UUID.nameUUIDFromBytes((EXTRA_PREFIX + attributeId.toString()).getBytes(StandardCharsets.UTF_8));
    }

    private static String modifierName(ResourceLocation attributeId) {
        return modifierId(attributeId).toString();
    }
}
