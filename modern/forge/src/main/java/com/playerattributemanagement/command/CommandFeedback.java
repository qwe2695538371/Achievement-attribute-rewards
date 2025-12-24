package com.playerattributemanagement.command;

import java.lang.reflect.Method;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public final class CommandFeedback {
    private static final Method SEND_SUCCESS_SUPPLIER;
    private static final Method SEND_SUCCESS_COMPONENT;

    static {
        Method supplier = null;
        Method component = null;
        try {
            supplier = CommandSourceStack.class.getMethod("sendSuccess", Supplier.class, boolean.class);
        } catch (NoSuchMethodException ignored) {
            // Older Forge uses sendSuccess(Component, boolean).
        }
        try {
            component = CommandSourceStack.class.getMethod("sendSuccess", Component.class, boolean.class);
        } catch (NoSuchMethodException ignored) {
            // Newer Forge uses sendSuccess(Supplier<Component>, boolean).
        }
        SEND_SUCCESS_SUPPLIER = supplier;
        SEND_SUCCESS_COMPONENT = component;
    }

    private CommandFeedback() {}

    @SuppressWarnings("unchecked")
    public static void sendSuccess(CommandSourceStack source, Component message, boolean broadcast) {
        if (source == null || message == null) {
            return;
        }
        try {
            if (SEND_SUCCESS_SUPPLIER != null) {
                SEND_SUCCESS_SUPPLIER.invoke(source, (Supplier<Component>) () -> message, broadcast);
                return;
            }
            if (SEND_SUCCESS_COMPONENT != null) {
                SEND_SUCCESS_COMPONENT.invoke(source, message, broadcast);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to send command feedback", e);
        }
    }
}
