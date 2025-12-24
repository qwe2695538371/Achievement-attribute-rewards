package com.playerattributemanagement.common.text;

import java.util.function.Function;

public final class DisplayNameResolver {
    private DisplayNameResolver() {}

    public record ResolvedName(String value, boolean translatable) {}

    public static <ID> ResolvedName resolve(
        ID id,
        String customName,
        Function<ID, String> translationKeyLookup,
        Function<ID, String> fallback
    ) {
        if (customName != null && !customName.isBlank()) {
            return new ResolvedName(customName, false);
        }
        if (translationKeyLookup != null) {
            String key = translationKeyLookup.apply(id);
            if (key != null && !key.isBlank()) {
                return new ResolvedName(key, true);
            }
        }
        String fallbackValue = fallback != null ? fallback.apply(id) : null;
        return new ResolvedName(fallbackValue != null ? fallbackValue : String.valueOf(id), false);
    }
}
