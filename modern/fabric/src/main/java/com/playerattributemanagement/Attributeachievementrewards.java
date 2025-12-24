// SPDX-License-Identifier: MIT
package com.playerattributemanagement;

import com.playerattributemanagement.attribute.ManagedAttributeIds;
import com.playerattributemanagement.config.AdvancementRewardConfig;
import com.playerattributemanagement.config.AttributePanelConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Attributeachievementrewards implements ModInitializer {
	public static final String MOD_ID = "playerattributemanagement";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Attribute Achievement Rewards (Fabric 1.20.1) init");
		AttributePanelConfig.get().loadOrCreate();
		ManagedAttributeIds.reload(AttributePanelConfig.get());
		AdvancementRewardConfig.load();
		ModNetworking.registerServerReceivers();
		PlayerAttributeCommand.register();
		ServerHooks.register();
	}
}
