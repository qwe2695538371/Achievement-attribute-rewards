package com.playerattributemanagement.config;

import com.playerattributemanagement.Playerattributemanagement;
import com.playerattributemanagement.attribute.AttributeIdMapper;
import com.playerattributemanagement.attribute.PlayerAttributeKeys;
import com.playerattributemanagement.common.attribute.AttributeKeyIndex;
import com.playerattributemanagement.common.config.AttributePanelConfigData;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

public final class AttributePanelConfig {
    private static final String FILE_NAME = Playerattributemanagement.MODID + "_panel.json";
    private static final AttributePanelConfig INSTANCE = new AttributePanelConfig();

    private boolean panelEnabled = true;
    private final Map<ResourceLocation, Boolean> attributeVisibility = new HashMap<>();
    private final Map<ResourceLocation, String> customAttributeNames = new HashMap<>();

    private AttributePanelConfig() {}

    public static AttributePanelConfig get() {
        return INSTANCE;
    }

    public void loadOrCreate() {
        Path path = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
        if (Files.exists(path)) {
            readExisting(path);
        } else {
            applyDefaults();
            writeConfig(path);
        }

        boolean changed = ensureAllAttributesPresent();
        if (changed) {
            writeConfig(path);
        }
    }

    public boolean isPanelEnabled() {
        return this.panelEnabled;
    }

    public boolean isAttributeVisible(ResourceLocation id) {
        ResourceLocation canonical = AttributeIdMapper.normalize(id);
        return this.attributeVisibility.getOrDefault(canonical, true);
    }

    public String getCustomDisplayName(ResourceLocation id) {
        ResourceLocation canonical = AttributeIdMapper.normalize(id);
        return this.customAttributeNames.get(canonical);
    }

    public Set<ResourceLocation> customAttributeIds() {
        return Collections.unmodifiableSet(this.customAttributeNames.keySet());
    }

    public Map<ResourceLocation, String> customAttributeDisplayNames() {
        return Collections.unmodifiableMap(this.customAttributeNames);
    }

    public Stream<ResourceLocation> visibleAttributeIds() {
        return orderedManagedIds().stream().filter(id -> this.attributeVisibility.getOrDefault(id, true));
    }

    public Stream<ResourceLocation> managedIdStream() {
        return orderedManagedIds().stream();
    }

    private void readExisting(Path path) {
        try {
            AttributePanelConfigData data = AttributePanelConfigData.read(path);
            this.panelEnabled = data.isPanelEnabled();
            this.attributeVisibility.clear();
            this.customAttributeNames.clear();
            data.getAttributeVisibility().forEach((key, value) -> {
                ResourceLocation rawId = ResourceLocation.tryParse(key);
                if (rawId != null) {
                    ResourceLocation canonical = AttributeIdMapper.normalize(rawId);
                    this.attributeVisibility.put(canonical, value);
                }
            });
            data.getCustomAttributeNames().forEach((key, value) -> {
                ResourceLocation rawId = ResourceLocation.tryParse(key);
                if (rawId == null) {
                    return;
                }
                if (value != null && !value.isBlank()) {
                    ResourceLocation canonical = AttributeIdMapper.normalize(rawId);
                    this.customAttributeNames.put(canonical, value);
                }
            });
        } catch (IOException e) {
            Playerattributemanagement.LOGGER.error("Failed to read attribute panel config; using defaults", e);
            applyDefaults();
        }
    }

    private void applyDefaults() {
        this.panelEnabled = true;
        this.attributeVisibility.clear();
        this.customAttributeNames.clear();
        PlayerAttributeKeys.baseManagedIds().forEach(id -> this.attributeVisibility.put(id, true));
    }

    private boolean ensureAllAttributesPresent() {
        boolean changed = false;
        for (ResourceLocation id : orderedManagedIds()) {
            if (!this.attributeVisibility.containsKey(id)) {
                this.attributeVisibility.put(id, true);
                changed = true;
            }
        }
        return changed;
    }

    private void writeConfig(Path path) {
        AttributePanelConfigData data = new AttributePanelConfigData();
        data.setPanelEnabled(this.panelEnabled);
        this.attributeVisibility.forEach((id, value) -> data.putAttributeVisibility(id.toString(), value));
        this.customAttributeNames.forEach((id, value) -> data.putCustomAttributeName(id.toString(), value));
        try {
            data.write(path);
        } catch (IOException e) {
            Playerattributemanagement.LOGGER.error("Failed to write attribute panel config", e);
        }
    }

    private List<ResourceLocation> orderedManagedIds() {
        List<String> orderedIds = AttributeKeyIndex.orderedManagedIds(
            PlayerAttributeKeys.baseManagedIds().map(ResourceLocation::toString).toList(),
            this.customAttributeNames.keySet().stream().map(ResourceLocation::toString).toList(),
            this.attributeVisibility.keySet().stream().map(ResourceLocation::toString).toList()
        );
        List<ResourceLocation> resolved = new ArrayList<>();
        for (String id : orderedIds) {
            ResourceLocation parsed = ResourceLocation.tryParse(id);
            if (parsed != null) {
                resolved.add(parsed);
            }
        }
        return List.copyOf(resolved);
    }
}
