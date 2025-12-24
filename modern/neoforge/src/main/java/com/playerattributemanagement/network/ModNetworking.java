package com.playerattributemanagement.network;

import com.playerattributemanagement.Playerattributemanagement;
import com.playerattributemanagement.data.PlayerAttributeDataHooks;
import com.playerattributemanagement.network.payload.RequestOpenAttributeScreenPayload;
import com.playerattributemanagement.network.payload.SyncPlayerAttributesPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Playerattributemanagement.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModNetworking {
    private ModNetworking() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Playerattributemanagement.MODID);
        registrar.playBidirectional(SyncPlayerAttributesPayload.TYPE, SyncPlayerAttributesPayload.STREAM_CODEC, ModNetworking::handleSyncPayload);
        registrar.playBidirectional(RequestOpenAttributeScreenPayload.TYPE, RequestOpenAttributeScreenPayload.STREAM_CODEC, ModNetworking::handleOpenRequest);
    }

    private static void handleSyncPayload(SyncPlayerAttributesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null) {
                    PlayerAttributeDataHooks.handleClientSync(payload);
                }
            }
        });
    }

    private static void handleOpenRequest(RequestOpenAttributeScreenPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                PlayerAttributeDataHooks.handleOpenRequest(serverPlayer);
            }
        });
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
}
