package com.playerattributemanagement.client;

import com.playerattributemanagement.common.text.DisplayNameResolver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

final class AttributeNameResolver {
	private AttributeNameResolver() {
	}

	public static Text resolve(Identifier id, String customName) {
		DisplayNameResolver.ResolvedName resolved = DisplayNameResolver.resolve(
			id,
			customName,
			attrId -> {
				if (MinecraftClient.getInstance().world != null) {
					EntityAttribute attribute = MinecraftClient.getInstance().world.getRegistryManager().get(RegistryKeys.ATTRIBUTE).get(attrId);
					return attribute != null ? attribute.getTranslationKey() : null;
				}
				return null;
			},
			Identifier::toString
		);
		return resolved.translatable()
			? Text.translatable(resolved.value())
			: Text.literal(resolved.value());
	}
}
