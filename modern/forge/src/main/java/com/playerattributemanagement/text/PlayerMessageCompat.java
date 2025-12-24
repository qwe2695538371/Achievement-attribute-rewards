package com.playerattributemanagement.text;

import java.lang.reflect.Method;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerMessageCompat {
    private static final Method SEND_SYSTEM_MESSAGE;
    private static final Method SEND_MESSAGE;
    private static final Method DISPLAY_CLIENT_MESSAGE;

    static {
        Method sendSystem = null;
        Method sendMessage = null;
        Method displayClient = null;
        try {
            sendSystem = ServerPlayer.class.getMethod("sendSystemMessage", Component.class);
        } catch (NoSuchMethodException ignored) {
            // Older versions use sendMessage or displayClientMessage.
        }
        try {
            sendMessage = ServerPlayer.class.getMethod("sendMessage", Component.class, UUID.class);
        } catch (NoSuchMethodException ignored) {
            // Fallback to displayClientMessage.
        }
        try {
            displayClient = ServerPlayer.class.getMethod("displayClientMessage", Component.class, boolean.class);
        } catch (NoSuchMethodException ignored) {
            // No fallback available.
        }
        SEND_SYSTEM_MESSAGE = sendSystem;
        SEND_MESSAGE = sendMessage;
        DISPLAY_CLIENT_MESSAGE = displayClient;
    }

    private PlayerMessageCompat() {}

    public static void sendSystem(ServerPlayer player, Component message) {
        if (player == null || message == null) {
            return;
        }
        try {
            if (SEND_SYSTEM_MESSAGE != null) {
                SEND_SYSTEM_MESSAGE.invoke(player, message);
                return;
            }
            if (SEND_MESSAGE != null) {
                SEND_MESSAGE.invoke(player, message, player.getUUID());
                return;
            }
            if (DISPLAY_CLIENT_MESSAGE != null) {
                DISPLAY_CLIENT_MESSAGE.invoke(player, message, false);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to send player message", e);
        }
    }
}
