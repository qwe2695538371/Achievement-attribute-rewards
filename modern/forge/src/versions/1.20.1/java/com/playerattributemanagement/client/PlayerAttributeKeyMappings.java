package com.playerattributemanagement.client;

import com.playerattributemanagement.Playerattributemanagement;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Playerattributemanagement.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
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
