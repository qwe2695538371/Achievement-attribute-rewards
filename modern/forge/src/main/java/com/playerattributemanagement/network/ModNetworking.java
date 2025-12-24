package com.playerattributemanagement.network;

import com.playerattributemanagement.Playerattributemanagement;
import com.playerattributemanagement.data.PlayerAttributeDataHooks;
import com.playerattributemanagement.network.payload.RequestOpenAttributeScreenPayload;
import com.playerattributemanagement.network.payload.SyncPlayerAttributesPayload;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(Playerattributemanagement.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;

    private ModNetworking() {}

    public static void register() {
        var syncBuilder = CHANNEL.messageBuilder(SyncPlayerAttributesPayload.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(SyncPlayerAttributesPayload::encode)
            .decoder(SyncPlayerAttributesPayload::decode);
        attachConsumer(syncBuilder, ModNetworking::handleSyncPayload);
        syncBuilder.add();

        var openBuilder = CHANNEL.messageBuilder(RequestOpenAttributeScreenPayload.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(RequestOpenAttributeScreenPayload::encode)
            .decoder(RequestOpenAttributeScreenPayload::decode);
        attachConsumer(openBuilder, ModNetworking::handleOpenRequest);
        openBuilder.add();
    }

    private static void handleSyncPayload(SyncPlayerAttributesPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                PlayerAttributeDataHooks.handleClientSync(payload);
            }
        }
        context.setPacketHandled(true);
    }

    private static void handleOpenRequest(RequestOpenAttributeScreenPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null) {
            PlayerAttributeDataHooks.handleOpenRequest(player);
        }
        context.setPacketHandled(true);
    }

    public static void sendToPlayer(ServerPlayer player, SyncPlayerAttributesPayload payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    public static void sendToServer(RequestOpenAttributeScreenPayload payload) {
        CHANNEL.sendToServer(payload);
    }

    private static <T> void attachConsumer(SimpleChannel.MessageBuilder builder, java.util.function.BiConsumer<T, Supplier<NetworkEvent.Context>> handler) {
        try {
            Method method = builder.getClass().getMethod("consumerMainThread", java.util.function.BiConsumer.class);
            method.invoke(builder, handler);
            return;
        } catch (NoSuchMethodException ignored) {
            // Fallback to older method name.
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register main thread handler", e);
        }

        try {
            Method method = builder.getClass().getMethod("consumer", java.util.function.BiConsumer.class);
            method.invoke(builder, handler);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to register network handler", e);
        }
    }
}
