package com.playerattributemanagement.network.payload;

import com.playerattributemanagement.common.network.AttributeSnapshot;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record SyncPlayerAttributesPayload(List<AttributeSnapshot> entries) {
    public SyncPlayerAttributesPayload {
        entries = List.copyOf(entries);
    }

    public static SyncPlayerAttributesPayload empty() {
        return new SyncPlayerAttributesPayload(List.of());
    }

    public static void encode(SyncPlayerAttributesPayload payload, FriendlyByteBuf buf) {
        buf.writeVarInt(payload.entries.size());
        for (AttributeSnapshot snapshot : payload.entries) {
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

    public static SyncPlayerAttributesPayload decode(FriendlyByteBuf buf) {
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
        return new ResourceLocation("minecraft", raw);
    }
}
