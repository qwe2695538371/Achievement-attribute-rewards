package com.playerattributemanagement.config;

import com.playerattributemanagement.Attributeachievementrewards;
import com.playerattributemanagement.attribute.AttributeIdMapper;
import com.playerattributemanagement.attribute.ManagedAttributeIds;
import com.playerattributemanagement.common.attribute.AttributeKeyIndex;
import com.playerattributemanagement.common.config.AttributePanelConfigData;
import com.playerattributemanagement.util.Identifiers;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

/**
 * 控制属性面板开关、可见属性和自定义显示名的简单 JSON 配置。
 */
public final class AttributePanelConfig {
	private static final String FILE_NAME = Attributeachievementrewards.MOD_ID + "_panel.json";
	private static final AttributePanelConfig INSTANCE = new AttributePanelConfig();

	private boolean panelEnabled = true;
	private final Map<Identifier, Boolean> attributeVisibility = new HashMap<>();
	private final Map<Identifier, String> customAttributeNames = new HashMap<>();

	private AttributePanelConfig() {
	}

	public static AttributePanelConfig get() {
		return INSTANCE;
	}

	public void loadOrCreate() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
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

	public boolean isAttributeVisible(Identifier id) {
		Identifier canonical = id;
		return this.attributeVisibility.getOrDefault(canonical, true);
	}

	public String getCustomDisplayName(Identifier id) {
		Identifier canonical = id;
		return this.customAttributeNames.get(canonical);
	}

	public Set<Identifier> customAttributeIds() {
		return Collections.unmodifiableSet(this.customAttributeNames.keySet());
	}

	public Map<Identifier, String> customAttributeDisplayNames() {
		return Collections.unmodifiableMap(this.customAttributeNames);
	}

	public Stream<Identifier> visibleAttributeIds() {
		return orderedManagedIds().stream().filter(id -> this.attributeVisibility.getOrDefault(id, true));
	}

	public Stream<Identifier> managedIdStream() {
		return orderedManagedIds().stream();
	}

	public List<Identifier> allManagedIds() {
		return orderedManagedIds();
	}

	public List<Identifier> visibleManagedIds() {
		return orderedManagedIds().stream().filter(id -> this.attributeVisibility.getOrDefault(id, true)).toList();
	}

	private void readExisting(Path path) {
		try {
			AttributePanelConfigData data = AttributePanelConfigData.read(path);
			this.panelEnabled = data.isPanelEnabled();
			this.attributeVisibility.clear();
			this.customAttributeNames.clear();
			data.getAttributeVisibility().forEach((key, value) -> {
				try {
					Identifier rawId = Identifiers.fromPath(key);
					Identifier canonical = AttributeIdMapper.normalize(rawId);
					if (canonical != null) {
						this.attributeVisibility.put(canonical, value);
					}
				} catch (Exception ignored) {
				}
			});
			data.getCustomAttributeNames().forEach((key, value) -> {
				try {
					Identifier rawId = Identifiers.fromPath(key);
					Identifier canonical = AttributeIdMapper.normalize(rawId);
					if (canonical != null && value != null && !value.isBlank()) {
						this.customAttributeNames.put(canonical, value);
					}
				} catch (Exception ignored) {
				}
			});
		} catch (IOException e) {
			Attributeachievementrewards.LOGGER.error("读取属性面板配置失败，使用默认配置", e);
			applyDefaults();
		}
	}

	private void applyDefaults() {
		this.panelEnabled = true;
		this.attributeVisibility.clear();
		this.customAttributeNames.clear();
		ManagedAttributeIds.baseIds().forEach(id -> this.attributeVisibility.put(id, true));
	}

	private boolean ensureAllAttributesPresent() {
		boolean changed = false;
		for (Identifier id : orderedManagedIds()) {
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
			Attributeachievementrewards.LOGGER.error("写入属性面板配置失败", e);
		}
	}

	private List<Identifier> orderedManagedIds() {
		List<String> orderedIds = AttributeKeyIndex.orderedManagedIds(
			ManagedAttributeIds.baseIds().stream().map(Identifier::toString).toList(),
			this.customAttributeNames.keySet().stream().map(Identifier::toString).toList(),
			this.attributeVisibility.keySet().stream().map(Identifier::toString).toList()
		);
		List<Identifier> resolved = new ArrayList<>();
		for (String id : orderedIds) {
			Identifier parsed = Identifier.tryParse(id);
			if (parsed != null) {
				resolved.add(parsed);
			}
		}
		return List.copyOf(resolved);
	}

}
