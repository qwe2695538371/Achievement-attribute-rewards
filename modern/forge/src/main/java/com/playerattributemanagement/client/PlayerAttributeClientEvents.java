package com.playerattributemanagement.client;

import com.playerattributemanagement.Playerattributemanagement;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Playerattributemanagement.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerAttributeClientEvents {
    private static final long HOTKEY_COOLDOWN_MS = 2000;
    private static long lastTriggerMs = 0;

    private PlayerAttributeClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
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
