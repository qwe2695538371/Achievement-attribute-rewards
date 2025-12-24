package com.playerattributemanagement.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AttributePanelConfigData {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private boolean panelEnabled = true;
    private final Map<String, Boolean> attributeVisibility = new HashMap<>();
    private final Map<String, String> customAttributeNames = new HashMap<>();

    public boolean isPanelEnabled() {
        return panelEnabled;
    }

    public void setPanelEnabled(boolean panelEnabled) {
        this.panelEnabled = panelEnabled;
    }

    public Map<String, Boolean> getAttributeVisibility() {
        return Collections.unmodifiableMap(attributeVisibility);
    }

    public Map<String, String> getCustomAttributeNames() {
        return Collections.unmodifiableMap(customAttributeNames);
    }

    public void clear() {
        attributeVisibility.clear();
        customAttributeNames.clear();
    }

    public void putAttributeVisibility(String id, boolean visible) {
        attributeVisibility.put(id, visible);
    }

    public void putCustomAttributeName(String id, String name) {
        customAttributeNames.put(id, name);
    }

    public void applyDefaults(Iterable<String> baseManagedIds) {
        panelEnabled = true;
        attributeVisibility.clear();
        customAttributeNames.clear();
        baseManagedIds.forEach(id -> attributeVisibility.put(id, true));
    }

    public boolean ensureAllAttributesPresent(Iterable<String> managedIds) {
        boolean changed = false;
        for (String id : managedIds) {
            if (!attributeVisibility.containsKey(id)) {
                attributeVisibility.put(id, true);
                changed = true;
            }
        }
        return changed;
    }

    public List<String> orderedManagedIds() {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        ordered.addAll(customAttributeNames.keySet());
        ordered.addAll(attributeVisibility.keySet());
        return List.copyOf(ordered);
    }

    public static AttributePanelConfigData read(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json == null) {
                throw new IOException("Empty panel config");
            }

            AttributePanelConfigData data = new AttributePanelConfigData();
            data.panelEnabled = json.has("panelEnabled") && json.get("panelEnabled").getAsBoolean();
            if (json.has("attributes") && json.get("attributes").isJsonObject()) {
                JsonObject attrs = json.getAsJsonObject("attributes");
                attrs.entrySet().forEach(entry -> data.attributeVisibility.put(entry.getKey(), entry.getValue().getAsBoolean()));
            }
            if (json.has("customAttributes") && json.get("customAttributes").isJsonObject()) {
                JsonObject custom = json.getAsJsonObject("customAttributes");
                custom.entrySet().forEach(entry -> {
                    String displayName = extractDisplayName(entry.getValue());
                    if (displayName != null && !displayName.isBlank()) {
                        data.customAttributeNames.put(entry.getKey(), displayName);
                    }
                });
            }

            return data;
        }
    }

    public void write(Path path) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("panelEnabled", panelEnabled);

        JsonObject attrs = new JsonObject();
        Comparator<String> keySorter = Comparator.naturalOrder();
        attributeVisibility.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(keySorter))
            .forEach(entry -> attrs.addProperty(entry.getKey(), entry.getValue()));
        root.add("attributes", attrs);

        JsonObject custom = new JsonObject();
        customAttributeNames.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(keySorter))
            .forEach(entry -> custom.addProperty(entry.getKey(), entry.getValue()));
        root.add("customAttributes", custom);

        Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }
    }

    private static String extractDisplayName(JsonElement element) {
        if (element == null) {
            return null;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return element.getAsString();
        }
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("displayName") && obj.get("displayName").isJsonPrimitive()) {
                return obj.getAsJsonPrimitive("displayName").getAsString();
            }
            if (obj.has("name") && obj.get("name").isJsonPrimitive()) {
                return obj.getAsJsonPrimitive("name").getAsString();
            }
        }
        return null;
    }
}
