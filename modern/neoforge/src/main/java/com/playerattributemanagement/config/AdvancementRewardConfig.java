package com.playerattributemanagement.config;

import com.playerattributemanagement.Playerattributemanagement;
import com.playerattributemanagement.attribute.AttributeIdMapper;
import com.playerattributemanagement.common.config.AdvancementRewardConfigData;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

public final class AdvancementRewardConfig {
    private static final String DEFAULT_REWARD_RESOURCE = "/defaults/playerattributemanagement_advancements.json";
    private static final String FILE_NAME = Playerattributemanagement.MODID + "_advancements.json";
    private static final AdvancementRewardConfig INSTANCE = new AdvancementRewardConfig();

    private final Map<ResourceLocation, Map<ResourceLocation, Double>> rewards = new HashMap<>();

    private AdvancementRewardConfig() {}

    public static AdvancementRewardConfig get() {
        return INSTANCE;
    }

    public void loadOrCreate() {
        Path path = configPath();
        if (Files.exists(path)) {
            readFrom(path);
        } else {
            Map<ResourceLocation, Map<ResourceLocation, Double>> defaults = defaultRewards();
            this.rewards.clear();
            this.rewards.putAll(defaults);
            writeTo(path);
        }
    }

    public Map<ResourceLocation, Double> getRewards(ResourceLocation advancementId) {
        return this.rewards.getOrDefault(advancementId, Collections.emptyMap());
    }

    private void readFrom(Path path) {
        try {
            AdvancementRewardConfigData data = AdvancementRewardConfigData.read(path);
            Map<ResourceLocation, Map<ResourceLocation, Double>> newMap = convertRewards(data.getRewards());
            this.rewards.clear();
            this.rewards.putAll(newMap);
        } catch (IOException e) {
            Playerattributemanagement.LOGGER.error("Failed to read advancement reward config; using empty defaults", e);
            this.rewards.clear();
        }
    }

    private void writeTo(Path path) {
        AdvancementRewardConfigData data = new AdvancementRewardConfigData();
        data.setRewards(toStringMap(this.rewards));
        try {
            data.write(path);
        } catch (IOException e) {
            Playerattributemanagement.LOGGER.error("Failed to write advancement reward config", e);
        }
    }

    private static ResourceLocation parseId(String raw) {
        ResourceLocation parsed = ResourceLocation.tryParse(raw);
        if (parsed != null) {
            return parsed;
        }

        try {
            return ResourceLocation.fromNamespaceAndPath("minecraft", raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Map<ResourceLocation, Map<ResourceLocation, Double>> defaultRewards() {
        Map<String, Map<String, Double>> defaults = loadDefaultRewards();
        if (defaults.isEmpty()) {
            defaults = AdvancementRewardConfigData.defaultRewards();
        }
        return convertRewards(defaults);
    }

    private static Map<String, Map<String, Double>> loadDefaultRewards() {
        try (InputStream input = AdvancementRewardConfig.class.getResourceAsStream(DEFAULT_REWARD_RESOURCE)) {
            if (input == null) {
                Playerattributemanagement.LOGGER.warn("Default reward table {} not found; using built-in defaults", DEFAULT_REWARD_RESOURCE);
                return Collections.emptyMap();
            }
            String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return AdvancementRewardConfigData.parseRewardsFromJson(json);
        } catch (IOException e) {
            Playerattributemanagement.LOGGER.warn("Failed to read default reward table {}; using built-in defaults", DEFAULT_REWARD_RESOURCE, e);
            return Collections.emptyMap();
        }
    }

    private static Path configPath() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }

    private static Map<ResourceLocation, Map<ResourceLocation, Double>> convertRewards(Map<String, Map<String, Double>> raw) {
        Map<ResourceLocation, Map<ResourceLocation, Double>> newMap = new LinkedHashMap<>();
        raw.forEach((advKey, attrMap) -> {
            ResourceLocation advId = parseId(advKey);
            if (advId == null) {
                Playerattributemanagement.LOGGER.warn("Skipping invalid advancement ID {}", advKey);
                return;
            }

            Map<ResourceLocation, Double> converted = new LinkedHashMap<>();
            attrMap.forEach((attrKey, value) -> {
                ResourceLocation rawAttrId = parseId(attrKey);
                if (rawAttrId == null) {
                    Playerattributemanagement.LOGGER.warn("Advancement {} contains invalid attribute key {}", advId, attrKey);
                    return;
                }
                ResourceLocation attrId = AttributeIdMapper.normalize(rawAttrId);
                converted.put(attrId, value);
            });

            newMap.put(advId, converted);
        });

        return newMap;
    }

    private static Map<String, Map<String, Double>> toStringMap(Map<ResourceLocation, Map<ResourceLocation, Double>> raw) {
        Map<String, Map<String, Double>> converted = new LinkedHashMap<>();
        raw.forEach((advId, attrMap) -> {
            Map<String, Double> inner = new LinkedHashMap<>();
            attrMap.forEach((attrId, value) -> inner.put(attrId.toString(), value));
            converted.put(advId.toString(), inner);
        });
        return converted;
    }
}
