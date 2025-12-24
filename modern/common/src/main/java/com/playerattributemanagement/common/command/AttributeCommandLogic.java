package com.playerattributemanagement.common.command;

import java.util.Collections;
import java.util.Map;

public final class AttributeCommandLogic {
    private AttributeCommandLogic() {}

    public enum Operation {
        SET,
        ADD,
        RESET,
        LIST
    }

    public interface Adapter<P, ID, C> {
        ID normalize(ID requested);
        boolean isValid(C context, ID canonical);
        double setExtra(P player, ID canonical, double value);
        double addExtra(P player, ID canonical, double delta);
        void resetExtra(P player, ID canonical);
        Map<ID, Double> listExtras(P player);
    }

    public record Result<ID>(boolean success, ID canonicalId, double value, Map<ID, Double> list) {
        public static <ID> Result<ID> invalid() {
            return new Result<>(false, null, 0.0, Collections.emptyMap());
        }
    }

    public static <P, ID, C> Result<ID> execute(
        Operation operation,
        C context,
        P player,
        ID requested,
        double value,
        Adapter<P, ID, C> adapter
    ) {
        if (operation == null || adapter == null) {
            return Result.invalid();
        }
        if (operation == Operation.LIST) {
            return new Result<>(true, null, 0.0, adapter.listExtras(player));
        }
        ID canonical = adapter.normalize(requested);
        if (canonical == null || !adapter.isValid(context, canonical)) {
            return Result.invalid();
        }

        switch (operation) {
            case SET -> {
                double newValue = adapter.setExtra(player, canonical, value);
                return new Result<>(true, canonical, newValue, Collections.emptyMap());
            }
            case ADD -> {
                double newValue = adapter.addExtra(player, canonical, value);
                return new Result<>(true, canonical, newValue, Collections.emptyMap());
            }
            case RESET -> {
                adapter.resetExtra(player, canonical);
                return new Result<>(true, canonical, 0.0, Collections.emptyMap());
            }
            case LIST -> {
                return new Result<>(true, canonical, 0.0, adapter.listExtras(player));
            }
            default -> {
                return Result.invalid();
            }
        }
    }
}
