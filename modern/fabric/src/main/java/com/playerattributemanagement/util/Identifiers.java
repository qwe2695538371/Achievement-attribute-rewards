package com.playerattributemanagement.util;

import java.util.Locale;
import net.minecraft.util.Identifier;

/**
 * Identifier helpers that avoid直接使用构造器，以便在多版本之间兼容。
 */
public final class Identifiers {
	private Identifiers() {
	}

	public static Identifier fromPath(String raw) {
		Identifier parsed = Identifier.tryParse(raw);
		if (parsed == null) {
			throw new IllegalArgumentException("无效的 Identifier: " + raw);
		}
		return parsed;
	}

	public static Identifier fromNamespacePath(String namespace, String path) {
		Identifier parsed = Identifier.of(namespace, path);
		if (parsed == null) {
			throw new IllegalArgumentException("无效的 Identifier: " + namespace + ":" + path);
		}
		return parsed;
	}

	public static Identifier namespaced(String namespace, Identifier attributeId, String suffix) {
		String safePath = attributeId.toUnderscoreSeparatedString();
		String combined = safePath + "_" + suffix;
		return fromNamespacePath(namespace, combined.toLowerCase(Locale.ROOT));
	}

	public static Identifier namespaced(String namespace, String path) {
		return fromNamespacePath(namespace, path);
	}
}
