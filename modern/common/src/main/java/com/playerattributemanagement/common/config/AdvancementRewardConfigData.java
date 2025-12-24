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
import java.util.LinkedHashMap;
import java.util.Map;

public final class AdvancementRewardConfigData {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DEFAULT_JSON = """
{
  "advancements": {
    "minecraft:husbandry/complete_catalogue": {
      "minecraft:generic.luck": 0.5
    },
    "minecraft:nether/brew_potion": {
      "minecraft:generic.luck": 0.2
    },
    "minecraft:husbandry/plant_any_sniffer_seed": {
      "minecraft:generic.luck": 0.1
    },
    "minecraft:story/form_obsidian": {
      "minecraft:generic.max_health": 2
    },
    "minecraft:story/deflect_arrow": {
      "minecraft:generic.knockback_resistance": 0.1
    },
    "minecraft:nether/netherite_armor": {
      "minecraft:generic.max_health": 4,
      "minecraft:generic.armor": 2
    },
    "minecraft:adventure/totem_of_undying": {
      "minecraft:generic.max_health": 2,
      "minecraft:generic.luck": 1
    },
    "minecraft:adventure/trade": {
      "minecraft:generic.armor": 1
    },
    "minecraft:husbandry/bred_all_animals": {
      "minecraft:generic.luck": 0.8
    },
    "minecraft:nether/uneasy_alliance": {
      "minecraft:generic.movement_speed": 0.01
    },
    "minecraft:adventure/hero_of_the_village": {
      "minecraft:generic.max_health": 2,
      "minecraft:generic.attack_speed": 1
    },
    "minecraft:nether/create_beacon": {
      "minecraft:generic.max_health": 2
    },
    "minecraft:adventure/lightning_rod_with_villager_no_fire": {
      "minecraft:generic.luck": 10
    },
    "minecraft:nether/summon_wither": {
      "minecraft:generic.max_health": 2
    },
    "minecraft:adventure/throw_trident": {
      "minecraft:generic.luck": 0.1
    },
    "minecraft:husbandry/tadpole_in_a_bucket": {
      "minecraft:generic.luck": 0.1
    },
    "minecraft:story/enchant_item": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:story/follow_ender_eye": {
      "minecraft:generic.max_health": 2
    },
    "minecraft:husbandry/leash_all_frog_variants": {
      "minecraft:generic.luck": 0.1
    },
    "minecraft:adventure/kill_a_mob": {
      "minecraft:generic.max_health": 1,
      "minecraft:generic.attack_speed": 0.5
    },
    "minecraft:nether/find_bastion": {
      "minecraft:generic.max_health": 2
    },
    "minecraft:nether/ride_strider": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:story/upgrade_tools": {
      "minecraft:generic.luck": 0.1
    },
    "minecraft:nether/return_to_sender": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:story/cure_zombie_villager": {
      "minecraft:generic.luck": 0.1
    },
    "minecraft:adventure/trim_with_all_exclusive_armor_patterns": {
      "minecraft:generic.luck": 0.1
    },
    "minecraft:adventure/honey_block_slide": {
      "minecraft:generic.luck": 2
    },
    "minecraft:story/enter_the_end": {
      "minecraft:generic.luck": 0.5
    },
    "minecraft:end/respawn_dragon": {
      "forge:reach_distance": 0.5
    },
    "minecraft:end/kill_dragon": {
      "minecraft:generic.max_health": 4,
      "forge:attack_range": 1
    },
    "minecraft:nether/distract_piglin": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/arbalistic": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:nether/get_wither_skull": {
      "forge:step_height_addition": 0.5
    },
    "minecraft:adventure/craft_decorated_pot_using_only_sherds": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/trim_with_any_armor_pattern": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/sniper_duel": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:husbandry/silk_touch_nest": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:husbandry/balanced_diet": {
      "minecraft:generic.max_health": 6
    },
    "minecraft:husbandry/plant_seed": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/ol_betsy": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:nether/all_potions": {
      "minecraft:generic.max_health": 6
    },
    "minecraft:story/smelt_iron": {
      "minecraft:generic.max_health": 2,
      "minecraft:generic.armor": 1
    },
    "minecraft:nether/charge_respawn_anchor": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/shoot_arrow": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/very_very_frightening": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/spyglass_at_dragon": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/spyglass_at_parrot": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/spyglass_at_ghast": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:end/dragon_breath": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/avoid_vibration": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/walk_on_powder_snow_with_leather_boots": {
      "minecraft:generic.luck": 0.1
    },
    "minecraft:nether/fast_travel": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:nether/explore_nether": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:story/lava_bucket": {
      "minecraft:generic.luck": -0.1
    },
    "minecraft:adventure/fall_from_world_height": {
      "minecraft:generic.max_health": 2,
      "minecraft:generic.luck": -0.3
    },
    "minecraft:nether/obtain_crying_obsidian": {
      "minecraft:generic.max_health": 0
    },
    "minecraft:nether/obtain_ancient_debris": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:story/mine_stone": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/kill_mob_near_sculk_catalyst": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/salvage_sherd": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/adventuring_time": {
      "minecraft:generic.max_health": 10
    },
    "minecraft:end/elytra": {
      "minecraft:generic.luck": 0.1
    },
    "minecraft:nether/use_lodestone": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/sleep_in_bed": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:husbandry/wax_on": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:husbandry/wax_off": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:nether/all_effects": {
      "minecraft:generic.max_health": 10,
      "minecraft:generic.attack_damage": 4
    },
    "minecraft:nether/ride_strider_in_overworld_lava": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:husbandry/breed_an_animal": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:nether/root": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:end/dragon_egg": {
      "minecraft:generic.max_health": 4
    },
    "minecraft:adventure/whos_the_pillager_now": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:husbandry/froglights": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:nether/create_full_beacon": {
      "minecraft:generic.max_health": 2
    },
    "minecraft:adventure/trade_at_world_height": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:husbandry/fishy_business": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:husbandry/make_a_sign_glow": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:husbandry/ride_a_boat_with_a_goat": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/two_birds_one_arrow": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:nether/find_fortress": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/play_jukebox_in_meadows": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:husbandry/tame_an_animal": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:story/enter_the_nether": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:husbandry/kill_axolotl_target": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:husbandry/safely_harvest_honey": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:nether/obtain_blaze_rod": {
      "minecraft:generic.armor": 1
    },
    "minecraft:end/enter_end_gateway": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:end/find_end_city": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:husbandry/tactical_fishing": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:nether/loot_bastion": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/summon_iron_golem": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:story/iron_tools": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:end/levitate": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:story/obtain_armor": {
      "minecraft:generic.armor": 1
    },
    "minecraft:adventure/bullseye": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/read_power_from_chiseled_bookshelf": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:husbandry/obtain_netherite_hoe": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:adventure/kill_all_mobs": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:story/mine_diamond": {
      "minecraft:generic.max_health": 1
    },
    "minecraft:story/shiny_gear": {
      "minecraft:generic.armor_toughness": 2,
      "minecraft:generic.armor": 2
    },
    "minecraft:husbandry/axolotl_in_a_bucket": {
      "minecraft:generic.max_health": 1
    }
  }
}
""";

    private final Map<String, Map<String, Double>> rewards = new LinkedHashMap<>();

    public Map<String, Map<String, Double>> getRewards() {
        return Collections.unmodifiableMap(rewards);
    }

    public void setRewards(Map<String, Map<String, Double>> rewards) {
        this.rewards.clear();
        this.rewards.putAll(rewards);
    }

    public static AdvancementRewardConfigData read(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            AdvancementRewardConfigData data = new AdvancementRewardConfigData();
            data.setRewards(parseRewards(root));
            return data;
        }
    }

    public void write(Path path) throws IOException {
        JsonObject root = new JsonObject();
        JsonObject advObj = new JsonObject();
        Comparator<String> sorter = Comparator.naturalOrder();

        rewards.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(sorter))
            .forEach(entry -> {
                JsonObject attrObj = new JsonObject();
                entry.getValue().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(sorter))
                    .forEach(attr -> attrObj.addProperty(attr.getKey(), attr.getValue()));
                advObj.add(entry.getKey(), attrObj);
            });

        root.add("advancements", advObj);

        Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }
    }

    public static Map<String, Map<String, Double>> defaultRewards() {
        JsonObject root = GSON.fromJson(DEFAULT_JSON, JsonObject.class);
        return parseRewards(root);
    }

    public static Map<String, Map<String, Double>> parseRewardsFromJson(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        JsonObject root = GSON.fromJson(json, JsonObject.class);
        return parseRewards(root);
    }

    private static Map<String, Map<String, Double>> parseRewards(JsonObject root) {
        if (root == null) {
            return Collections.emptyMap();
        }

        JsonObject advObj = root;
        if (root.has("advancements") && root.get("advancements").isJsonObject()) {
            advObj = root.getAsJsonObject("advancements");
        }

        Map<String, Map<String, Double>> newMap = new LinkedHashMap<>();
        advObj.entrySet().forEach(entry -> {
            JsonElement element = entry.getValue();
            if (!element.isJsonObject()) {
                return;
            }

            Map<String, Double> attrMap = new LinkedHashMap<>();
            element.getAsJsonObject().entrySet().forEach(attrEntry -> {
                try {
                    attrMap.put(attrEntry.getKey(), attrEntry.getValue().getAsDouble());
                } catch (Exception ignored) {
                }
            });
            newMap.put(entry.getKey(), attrMap);
        });

        return newMap;
    }
}
