package com.playerattributemanagement.network.payload;

import net.minecraft.network.FriendlyByteBuf;

public record RequestOpenAttributeScreenPayload() {
    public static void encode(RequestOpenAttributeScreenPayload payload, FriendlyByteBuf buf) {
        // nothing to encode
    }

    public static RequestOpenAttributeScreenPayload decode(FriendlyByteBuf buf) {
        return new RequestOpenAttributeScreenPayload();
    }
}
