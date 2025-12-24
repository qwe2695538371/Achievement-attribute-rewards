package com.playerattributemanagement.mixin;

import com.playerattributemanagement.reward.AdvancementRewardHandler;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
	@Shadow @Final private ServerPlayerEntity owner;

	@Inject(method = "grantCriterion", at = @At("RETURN"))
	private void attributeRewards$onGrant(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) {
			return;
		}
		Identifier id = advancement.getId();
		if (id != null) {
			AdvancementRewardHandler.handle(owner, id);
		}
	}
}
