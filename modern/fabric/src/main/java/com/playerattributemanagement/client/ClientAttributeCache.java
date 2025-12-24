package com.playerattributemanagement.client;

import com.playerattributemanagement.common.network.AttributeSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClientAttributeCache {
	private static List<AttributeSnapshot> entries = new ArrayList<>();

	private ClientAttributeCache() {
	}

	public static List<AttributeSnapshot> get() {
		return Collections.unmodifiableList(entries);
	}

	public static void set(List<AttributeSnapshot> newEntries) {
		entries = new ArrayList<>(newEntries);
	}
}
