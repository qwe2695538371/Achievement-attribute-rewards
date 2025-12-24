package com.playerattributemanagement.client;

import com.playerattributemanagement.config.AttributePanelConfig;
import com.playerattributemanagement.network.ModNetworking;
import com.playerattributemanagement.network.payload.RequestOpenAttributeScreenPayload;
import com.playerattributemanagement.network.payload.SyncPlayerAttributesPayload;
import com.playerattributemanagement.client.gui.PlayerAttributeScreen;
import net.minecraft.client.Minecraft;

public final class PlayerAttributeClientBridge {
    private static boolean awaitingScreen;

    private PlayerAttributeClientBridge() {}

    public static void requestOpenScreen() {
        if (!AttributePanelConfig.get().isPanelEnabled()) {
            return;
        }

        awaitingScreen = true;
        ModNetworking.sendToServer(new RequestOpenAttributeScreenPayload());
    }

    public static void acceptSnapshot(SyncPlayerAttributesPayload payload) {
        if (!AttributePanelConfig.get().isPanelEnabled()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientAttributeCache.get().update(payload);
        if (awaitingScreen) {
            awaitingScreen = false;
            minecraft.setScreen(new PlayerAttributeScreen());
        } else if (minecraft.screen instanceof PlayerAttributeScreen screen) {
            screen.reloadData();
        }
    }
}
