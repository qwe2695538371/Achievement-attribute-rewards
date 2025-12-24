package com.playerattributemanagement.reward;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;

@EventBusSubscriber(modid = Playerattributemanagement.MODID)
public final class AdvancementRewardHandler {
    private AdvancementRewardHandler() {}

    @SubscribeEvent
    public static void onAdvancementEarn(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (event.getAdvancement().value().display().isEmpty()) {
            return;
        }

        ResourceLocation advancementId = event.getAdvancement().id();
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
                Attribute attribute = AttributeResolver.resolveAttribute(id, player.level().registryAccess());
                return attribute != null ? attribute.getDescriptionId() : null;
            },
            ResourceLocation::toString
        );
        net.minecraft.network.chat.Component attrComponent = resolved.translatable()
            ? net.minecraft.network.chat.Component.translatable(resolved.value())
            : net.minecraft.network.chat.Component.literal(resolved.value());

        String formatted = RewardFormat.formatSigned(amount);

        player.sendSystemMessage(
            net.minecraft.network.chat.Component.translatable(
                "message.playerattributemanagement.advancement_reward",
                player.getDisplayName(),
                attrComponent,
                formatted
            )
        );
    }
}





