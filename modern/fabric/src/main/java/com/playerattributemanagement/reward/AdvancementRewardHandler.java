package com.playerattributemanagement.reward;

import com.playerattributemanagement.attribute.AttributeIdMapper;
import com.playerattributemanagement.attribute.PlayerAttributeStore;
import com.playerattributemanagement.common.reward.RewardFormat;
import com.playerattributemanagement.common.reward.RewardProcessor;
import com.playerattributemanagement.common.text.DisplayNameResolver;
import com.playerattributemanagement.config.AdvancementRewardConfig;
import com.playerattributemanagement.config.AttributePanelConfig;
import java.util.Map;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class AdvancementRewardHandler {
	private AdvancementRewardHandler() {
	}

	public static void handle(ServerPlayerEntity player, Identifier advancementId) {
		Map<Identifier, Double> rewards = AdvancementRewardConfig.forAdvancement(advancementId);
		if (rewards.isEmpty()) {
			return;
		}

		Map<Identifier, Double> granted = RewardProcessor.normalizeRewards(rewards, AttributeIdMapper::normalize);
		if (granted.isEmpty()) {
			return;
		}

		PlayerAttributeStore.addExtras(player, granted);
		granted.forEach((attrId, delta) -> {
			Text attrName = resolveAttributeName(attrId);
			player.sendMessage(
				Text.translatable(
					"message.playerattributemanagement.advancement_reward",
					attrName,
					RewardFormat.formatSigned(delta)
				),
				false
			);
		});
	}

	private static Text resolveAttributeName(Identifier id) {
		String customName = AttributePanelConfig.get().getCustomDisplayName(id);
		DisplayNameResolver.ResolvedName resolved = DisplayNameResolver.resolve(
			id,
			customName,
			attrId -> Registries.ATTRIBUTE.containsId(attrId)
				? Registries.ATTRIBUTE.get(attrId).getTranslationKey()
				: null,
			Identifier::toString
		);
		return resolved.translatable()
			? Text.translatable(resolved.value())
			: Text.literal(resolved.value());
	}

}
