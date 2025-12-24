package com.playerattributemanagement.network.payload;

import com.playerattributemanagement.Playerattributemanagement;
import com.playerattributemanagement.common.network.AttributeSnapshot;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncPlayerAttributesPayload(List<AttributeSnapshot> entries) implements CustomPacketPayload {
    public static final Type<SyncPlayerAttributesPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(Playerattributemanagement.MODID, "sync_player_attributes")
    );
    public static final StreamCodec<FriendlyByteBuf, SyncPlayerAttributesPayload> STREAM_CODEC = StreamCodec.ofMember(
        SyncPlayerAttributesPayload::write, SyncPlayerAttributesPayload::read
    );

    public SyncPlayerAttributesPayload {
        entries = List.copyOf(entries);
    }

    public static SyncPlayerAttributesPayload empty() {
        return new SyncPlayerAttributesPayload(List.of());
    }

    @Override
    public Type<SyncPlayerAttributesPayload> type() {
        return TYPE;
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entries.size());
        for (AttributeSnapshot snapshot : this.entries) {
            ResourceLocation id = parseId(snapshot.id());
            buf.writeResourceLocation(id);
            buf.writeDouble(snapshot.baseValue());
            buf.writeDouble(snapshot.extraValue());
            buf.writeDouble(snapshot.totalValue());
            String customName = snapshot.customName();
            boolean hasCustom = customName != null && !customName.isBlank();
            buf.writeBoolean(hasCustom);
            if (hasCustom) {
                buf.writeUtf(customName);
            }
        }
    }

    private static SyncPlayerAttributesPayload read(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<AttributeSnapshot> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            double base = buf.readDouble();
            double extra = buf.readDouble();
            double total = buf.readDouble();
            String customName = buf.readBoolean() ? buf.readUtf() : null;
            entries.add(new AttributeSnapshot(id.toString(), base, extra, total, customName));
        }

        return new SyncPlayerAttributesPayload(entries);
    }

    private static ResourceLocation parseId(String raw) {
        ResourceLocation parsed = ResourceLocation.tryParse(raw);
        if (parsed != null) {
            return parsed;
        }
        return ResourceLocation.fromNamespaceAndPath("minecraft", raw);
    }
}
