package com.playerattributemanagement.config;

import com.playerattributemanagement.Attributeachievementrewards;
import com.playerattributemanagement.attribute.AttributeIdMapper;
import com.playerattributemanagement.common.config.AdvancementRewardConfigData;
import com.playerattributemanagement.util.Identifiers;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public final class AdvancementRewardConfig {
	private static final String FILE_NAME = Attributeachievementrewards.MOD_ID + "_advancements.json";
	private static Map<Identifier, Map<Identifier, Double>> rewards = Collections.emptyMap();

	private AdvancementRewardConfig() {
	}

	public static void load() {
		Path file = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		try {
			Files.createDirectories(file.getParent());
			if (!Files.exists(file)) {
				writeDefault(file);
			}
			read(file);
		} catch (IOException e) {
			Attributeachievementrewards.LOGGER.warn("加载成就奖励配置失败，将使用空配置", e);
			rewards = Collections.emptyMap();
		}
	}

	public static Map<Identifier, Double> forAdvancement(Identifier id) {
		return rewards.getOrDefault(id, Collections.emptyMap());
	}

	private static void read(Path file) throws IOException {
		AdvancementRewardConfigData data = AdvancementRewardConfigData.read(file);
		rewards = convertRewards(data.getRewards());
	}

	private static void writeDefault(Path file) throws IOException {
		AdvancementRewardConfigData data = new AdvancementRewardConfigData();
		data.setRewards(AdvancementRewardConfigData.defaultRewards());
		data.write(file);
	}

	private static Map<Identifier, Map<Identifier, Double>> convertRewards(Map<String, Map<String, Double>> raw) {
		Map<Identifier, Map<Identifier, Double>> parsed = new LinkedHashMap<>();
		raw.forEach((advKey, attrMap) -> {
			Identifier advId = parseId(advKey);
			if (advId == null) {
				return;
			}
			Map<Identifier, Double> converted = new LinkedHashMap<>();
			attrMap.forEach((attrKey, value) -> {
				Identifier attrId = parseId(attrKey);
				if (attrId != null) {
					converted.put(attrId, value);
				}
			});
			parsed.put(advId, converted);
		});
		return parsed;
	}

	private static Identifier parseId(String raw) {
		Identifier parsed = Identifier.tryParse(raw);
		if (parsed == null) {
			try {
				parsed = Identifiers.fromNamespacePath("minecraft", raw);
			} catch (Exception ignored) {
				return null;
			}
		}
		return AttributeIdMapper.normalize(parsed);
	}
}
