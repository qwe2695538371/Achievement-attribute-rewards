package com.playerattributemanagement.text;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import net.minecraft.network.chat.Component;

public final class TextCompat {
    private static final Method LITERAL_METHOD;
    private static final Method TRANSLATABLE_METHOD;
    private static final Constructor<?> TEXT_COMPONENT_CTOR;
    private static final Constructor<?> TRANSLATABLE_COMPONENT_CTOR;

    static {
        Method literal = null;
        Method translatable = null;
        Constructor<?> textCtor = null;
        Constructor<?> translatableCtor = null;
        try {
            literal = Component.class.getMethod("literal", String.class);
        } catch (NoSuchMethodException ignored) {
            // 1.18.x does not expose Component.literal.
        }
        try {
            translatable = Component.class.getMethod("translatable", String.class, Object[].class);
        } catch (NoSuchMethodException ignored) {
            // 1.18.x does not expose Component.translatable.
        }
        try {
            Class<?> textComponent = Class.forName("net.minecraft.network.chat.TextComponent");
            textCtor = textComponent.getConstructor(String.class);
        } catch (ReflectiveOperationException ignored) {
            // Newer versions may not need this fallback.
        }
        try {
            Class<?> translatableComponent = Class.forName("net.minecraft.network.chat.TranslatableComponent");
            translatableCtor = translatableComponent.getConstructor(String.class, Object[].class);
        } catch (ReflectiveOperationException ignored) {
            // Newer versions may not need this fallback.
        }
        LITERAL_METHOD = literal;
        TRANSLATABLE_METHOD = translatable;
        TEXT_COMPONENT_CTOR = textCtor;
        TRANSLATABLE_COMPONENT_CTOR = translatableCtor;
    }

    private TextCompat() {}

    public static Component literal(String text) {
        String safe = text == null ? "" : text;
        try {
            if (LITERAL_METHOD != null) {
                return (Component) LITERAL_METHOD.invoke(null, safe);
            }
            if (TEXT_COMPONENT_CTOR != null) {
                return (Component) TEXT_COMPONENT_CTOR.newInstance(safe);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to build literal component", e);
        }
        throw new IllegalStateException("No available literal component factory");
    }

    public static Component translatable(String key, Object... args) {
        String safe = key == null ? "" : key;
        Object[] safeArgs = args == null ? new Object[0] : args;
        try {
            if (TRANSLATABLE_METHOD != null) {
                return (Component) TRANSLATABLE_METHOD.invoke(null, safe, safeArgs);
            }
            if (TRANSLATABLE_COMPONENT_CTOR != null) {
                return (Component) TRANSLATABLE_COMPONENT_CTOR.newInstance(safe, safeArgs);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to build translatable component", e);
        }
        throw new IllegalStateException("No available translatable component factory");
    }
}
