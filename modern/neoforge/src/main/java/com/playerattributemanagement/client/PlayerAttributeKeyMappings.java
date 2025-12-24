package com.playerattributemanagement.client;

import com.playerattributemanagement.Playerattributemanagement;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Playerattributemanagement.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class PlayerAttributeKeyMappings {
    private static final KeyMapping ATTRIBUTE_PANEL_KEY = new KeyMapping(
        "key.playerattributemanagement.attribute_panel",
        GLFW.GLFW_KEY_O,
        "key.categories.playerattributemanagement"
    );

    private PlayerAttributeKeyMappings() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(ATTRIBUTE_PANEL_KEY);
    }

    static boolean consumePanelClick() {
        return ATTRIBUTE_PANEL_KEY.consumeClick();
    }
}
