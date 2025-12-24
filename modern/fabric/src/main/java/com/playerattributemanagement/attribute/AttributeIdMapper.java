package com.playerattributemanagement.attribute;

import com.playerattributemanagement.common.attribute.AttributeIdAliasMap;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;

public final class AttributeIdMapper {
    private static final AttributeIdAliasMap ALIASES = AttributeIdAliasMap.fromCanonicalIds(baseManagedIds(), null);

    private AttributeIdMapper() {
    }

    public static Identifier normalize(Identifier id) {
        if (id == null) {
            return null;
        }
        String normalized = ALIASES.normalize(id.toString());
        return normalized != null ? Identifier.tryParse(normalized) : null;
    }

    private static Set<String> baseManagedIds() {
        return ManagedAttributeIds.baseIds().stream()
            .map(Identifier::toString)
            .collect(Collectors.toSet());
    }
}
