package com.playerattributemanagement.client;

import com.playerattributemanagement.Playerattributemanagement;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Playerattributemanagement.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class PlayerAttributeClientEvents {
    private static final long HOTKEY_COOLDOWN_MS = 2000;
    private static long lastTriggerMs = 0;

    private PlayerAttributeClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (PlayerAttributeKeyMappings.consumePanelClick()) {
            long now = Util.getMillis();
            if (now - lastTriggerMs < HOTKEY_COOLDOWN_MS) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                lastTriggerMs = now;
                PlayerAttributeClientBridge.requestOpenScreen();
            }
        }
    }
}
