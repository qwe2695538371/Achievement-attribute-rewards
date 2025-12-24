package com.playerattributemanagement.attribute;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * ????????? Minecraft ?????????????
 */
public final class AttributeResolver {
    private AttributeResolver() {}

    public static Attribute resolveAttribute(ResourceLocation id, RegistryAccess access) {
        if (id == null) {
            return null;
        }
        return ForgeRegistries.ATTRIBUTES.getValue(id);
    }

    public static boolean exists(ResourceLocation id, RegistryAccess access) {
        return resolveAttribute(id, access) != null;
    }
}
