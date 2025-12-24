package com.playerattributemanagement.attribute;

import com.playerattributemanagement.common.attribute.AttributeIdAliasMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;

public final class AttributeIdMapper {
    private static final Map<String, String> CUSTOM_ALIASES = Map.of(
        "forge:reach_distance",
        "forge:block_reach",
        "forge:step_height_addition",
        "forge:step_height"
    );
    private static final AttributeIdAliasMap ALIASES = AttributeIdAliasMap.fromCanonicalIds(baseManagedIds(), CUSTOM_ALIASES);

    private AttributeIdMapper() {}

    public static ResourceLocation normalize(ResourceLocation id) {
        if (id == null) {
            return null;
        }
        String normalized = ALIASES.normalize(id.toString());
        return normalized != null ? ResourceLocation.tryParse(normalized) : null;
    }

    private static Set<String> baseManagedIds() {
        return PlayerAttributeKeys.baseManagedIds()
            .map(ResourceLocation::toString)
            .collect(Collectors.toSet());
    }
}
