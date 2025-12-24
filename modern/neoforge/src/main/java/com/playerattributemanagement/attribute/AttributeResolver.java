package com.playerattributemanagement.attribute;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

/**
 * 统一处理不同 Minecraft 版本的属性解析。
 */
public final class AttributeResolver {
    private AttributeResolver() {}

    public static Holder<Attribute> resolveHolder(ResourceLocation id, RegistryAccess access) {
        if (id == null || access == null) {
            return null;
        }
        HolderLookup.RegistryLookup<Attribute> lookup = access.lookupOrThrow(Registries.ATTRIBUTE);
        ResourceKey<Attribute> key = ResourceKey.create(Registries.ATTRIBUTE, id);
        return lookup.get(key).orElse(null);
    }

    public static Attribute resolveAttribute(ResourceLocation id, RegistryAccess access) {
        Holder<Attribute> holder = resolveHolder(id, access);
        return holder != null ? holder.value() : null;
    }

    public static boolean exists(ResourceLocation id, RegistryAccess access) {
        return resolveHolder(id, access) != null;
    }
}
