package com.playerattributemanagement.network.payload;

import com.playerattributemanagement.Playerattributemanagement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RequestOpenAttributeScreenPayload() implements CustomPacketPayload {
    public static final Type<RequestOpenAttributeScreenPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Playerattributemanagement.MODID, "request_attribute_screen")
    );
    public static final StreamCodec<FriendlyByteBuf, RequestOpenAttributeScreenPayload> STREAM_CODEC = StreamCodec.unit(new RequestOpenAttributeScreenPayload());

    @Override
    public Type<RequestOpenAttributeScreenPayload> type() {
        return TYPE;
    }
}
