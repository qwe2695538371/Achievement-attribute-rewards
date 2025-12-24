package com.playerattributemanagement.common.network;

public record AttributeSnapshot(String id, double baseValue, double extraValue, double totalValue, String customName) {
    public AttributeSnapshot {
        id = id == null ? "" : id;
    }
}
