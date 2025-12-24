package com.playerattributemanagement.reward;

import com.playerattributemanagement.text.PlayerMessageCompat;
import com.playerattributemanagement.text.TextCompat;
import com.playerattributemanagement.Playerattributemanagement;
import com.playerattributemanagement.attribute.AttributeIdMapper;
import com.playerattributemanagement.attribute.AttributeResolver;
import com.playerattributemanagement.attribute.PlayerAttributeApplier;
import com.playerattributemanagement.attribute.PlayerAttributeStore;
import com.playerattributemanagement.common.reward.RewardFormat;
import com.playerattributemanagement.common.reward.RewardProcessor;
import com.playerattributemanagement.common.text.DisplayNameResolver;
import com.playerattributemanagement.config.AdvancementRewardConfig;
import com.playerattributemanagement.config.AttributePanelConfig;
import com.playerattributemanagement.data.PlayerAttributeDataHooks;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Playerattributemanagement.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AdvancementRewardHandler {
    private AdvancementRewardHandler() {}

    @SubscribeEvent
    public static void onAdvancementEarn(AdvancementEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Advancement advancement = event.getAdvancement();
        if (advancement.getDisplay() == null) {
            return;
        }

        ResourceLocation advancementId = advancement.getId();
        Map<ResourceLocation, Double> rewards = AdvancementRewardConfig.get().getRewards(advancementId);
        if (rewards.isEmpty()) {
            return;
        }

        Map<ResourceLocation, Double> granted = RewardProcessor.normalizeRewards(rewards, AttributeIdMapper::normalize);

        if (granted.isEmpty()) {
            return;
        }

        PlayerAttributeStore store = PlayerAttributeDataHooks.readStore(player);
        List<Map.Entry<ResourceLocation, Double>> applied = new ArrayList<>();
        granted.forEach((attributeId, amount) -> {
            double newValue = store.getExtra(attributeId) + amount;
            store.setExtra(attributeId, newValue);
            applied.add(Map.entry(attributeId, amount));
        });

        if (applied.isEmpty()) {
            return;
        }

        PlayerAttributeApplier.applyAndClean(player, store);
        PlayerAttributeDataHooks.saveStore(player, store);
        PlayerAttributeDataHooks.syncToClient(player, store);
        applied.forEach(entry -> announceReward(player, entry.getKey(), entry.getValue()));
    }

    private static void announceReward(ServerPlayer player, ResourceLocation attributeId, double amount) {
        String customName = AttributePanelConfig.get().getCustomDisplayName(attributeId);
        DisplayNameResolver.ResolvedName resolved = DisplayNameResolver.resolve(
            attributeId,
            customName,
            id -> {
                Attribute attribute = AttributeResolver.resolveAttribute(id, null);
                return attribute != null ? attribute.getDescriptionId() : null;
            },
            ResourceLocation::toString
        );
        Component attrComponent = resolved.translatable()
            ? TextCompat.translatable(resolved.value())
            : TextCompat.literal(resolved.value());

        String formatted = RewardFormat.formatSigned(amount);

        PlayerMessageCompat.sendSystem(
            player,
            TextCompat.translatable(
                "message.playerattributemanagement.advancement_reward",
                player.getDisplayName(),
                attrComponent,
                formatted
            )
        );
    }
}
