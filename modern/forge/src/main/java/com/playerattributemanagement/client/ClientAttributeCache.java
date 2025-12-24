package com.playerattributemanagement.client;

import com.playerattributemanagement.attribute.PlayerAttributeKeys;
import com.playerattributemanagement.common.network.AttributeSnapshot;
import com.playerattributemanagement.network.payload.SyncPlayerAttributesPayload;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ClientAttributeCache {
    private static final ClientAttributeCache INSTANCE = new ClientAttributeCache();

    private final Map<String, AttributeSnapshot> snapshotMap = new LinkedHashMap<>();

    private ClientAttributeCache() {}

    public static ClientAttributeCache get() {
        return INSTANCE;
    }

    public void update(SyncPlayerAttributesPayload payload) {
        this.snapshotMap.clear();
        payload.entries().forEach(entry -> this.snapshotMap.put(entry.id(), entry));
    }

    public List<AttributeSnapshot> orderedEntries() {
        List<AttributeSnapshot> ordered = new ArrayList<>();
        Set<String> handled = new LinkedHashSet<>();
        PlayerAttributeKeys.managedIds().forEach(id -> {
            String key = id.toString();
            AttributeSnapshot snapshot = this.snapshotMap.get(key);
            if (snapshot != null) {
                ordered.add(snapshot);
                handled.add(key);
            }
        });

        this.snapshotMap.forEach((id, snapshot) -> {
            if (!handled.contains(id)) {
                ordered.add(snapshot);
            }
        });
        return ordered;
    }
}
