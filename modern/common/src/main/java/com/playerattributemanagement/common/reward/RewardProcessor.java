package com.playerattributemanagement.common.reward;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public final class RewardProcessor {
    private RewardProcessor() {}

    public static <ID> Map<ID, Double> normalizeRewards(Map<ID, Double> raw, Function<ID, ID> normalizer) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }

        Map<ID, Double> merged = new LinkedHashMap<>();
        raw.forEach((id, amount) -> {
            if (RewardFormat.isZero(amount)) {
                return;
            }
            ID normalized = normalizer != null ? normalizer.apply(id) : id;
            if (normalized == null) {
                return;
            }
            merged.merge(normalized, amount, Double::sum);
        });

        merged.entrySet().removeIf(entry -> RewardFormat.isZero(entry.getValue()));
        return merged;
    }
}
