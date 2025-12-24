package com.playerattributemanagement.attribute;

import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/**
 * 兼容 1.21.1/1.21.5 之间 NBT API 签名差异的辅助工具。
 * <p>
 * 1.21.5 开始 CompoundTag#contains/getString/getDouble/getList 返回 Optional
 * 且不再接受 tag 类型参数；旧版本仍保留原始签名。此处用反射兜底，保持单套源码可编译。
 */
public final class NbtCompat {
    private static final Method CONTAINS_WITH_TYPE = findMethod(CompoundTag.class, "contains", String.class, int.class);
    private static final Method CONTAINS_SIMPLE = findMethod(CompoundTag.class, "contains", String.class);
    private static final Method GET_LIST_WITH_TYPE = findMethod(CompoundTag.class, "getList", String.class, int.class);
    private static final Method GET_LIST_SIMPLE = findMethod(CompoundTag.class, "getList", String.class);
    private static final Method GET_STRING = findMethod(CompoundTag.class, "getString", String.class);
    private static final Method GET_DOUBLE = findMethod(CompoundTag.class, "getDouble", String.class);
    private static final Method GET_COMPOUND = findMethod(CompoundTag.class, "getCompound", String.class);

    private NbtCompat() {}

    public static boolean has(CompoundTag tag, String key, int expectedType) {
        Boolean withType = invokeBoolean(CONTAINS_WITH_TYPE, tag, key, expectedType);
        if (withType != null) {
            return withType;
        }
        Boolean simple = invokeBoolean(CONTAINS_SIMPLE, tag, key);
        return simple != null && simple;
    }

    public static ListTag getList(CompoundTag tag, String key, int expectedType) {
        ListTag withType = invokeList(GET_LIST_WITH_TYPE, tag, key, expectedType);
        if (withType != null) {
            return withType;
        }
        ListTag simple = invokeList(GET_LIST_SIMPLE, tag, key);
        return simple != null ? simple : new ListTag();
    }

    public static String getString(CompoundTag tag, String key) {
        Object result = invoke(GET_STRING, tag, key);
        if (result instanceof String s) {
            return s;
        }
        if (result instanceof Optional<?> optional) {
            Object value = optional.orElse(null);
            if (value instanceof String s) {
                return s;
            }
        }
        return "";
    }

    public static double getDouble(CompoundTag tag, String key) {
        Object result = invoke(GET_DOUBLE, tag, key);
        if (result instanceof Double d) {
            return d;
        }
        if (result instanceof Optional<?> optional) {
            Object value = optional.orElse(null);
            if (value instanceof Double d) {
                return d;
            }
            if (value instanceof Number num) {
                return num.doubleValue();
            }
        }
        return 0.0;
    }

    public static CompoundTag getCompound(CompoundTag tag, String key) {
        Object result = invoke(GET_COMPOUND, tag, key);
        if (result instanceof CompoundTag compound) {
            return compound;
        }
        if (result instanceof Optional<?> optional) {
            Object value = optional.orElse(null);
            if (value instanceof CompoundTag compound) {
                return compound;
            }
        }
        return null;
    }

    private static Boolean invokeBoolean(Method method, Object target, Object... args) {
        Object result = invoke(method, target, args);
        if (result instanceof Boolean b) {
            return b;
        }
        if (result instanceof Optional<?> optional) {
            Object value = optional.orElse(null);
            if (value instanceof Boolean b) {
                return b;
            }
        }
        return null;
    }

    private static ListTag invokeList(Method method, Object target, Object... args) {
        Object result = invoke(method, target, args);
        if (result instanceof ListTag listTag) {
            return listTag;
        }
        if (result instanceof Optional<?> optional) {
            Object value = optional.orElse(null);
            if (value instanceof ListTag listTag) {
                return listTag;
            }
        }
        return null;
    }

    private static Object invoke(Method method, Object target, Object... args) {
        if (method == null || target == null) {
            return null;
        }
        try {
            return method.invoke(target, args);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static Method findMethod(Class<?> owner, String name, Class<?>... types) {
        try {
            Method method = owner.getMethod(name, types);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
