package com.playerattributemanagement.data;

import com.playerattributemanagement.Playerattributemanagement;
import com.playerattributemanagement.attribute.AttributeResolver;
import com.playerattributemanagement.attribute.PlayerAttributeApplier;
import com.playerattributemanagement.attribute.PlayerAttributeKeys;
import com.playerattributemanagement.attribute.PlayerAttributeStore;
import com.playerattributemanagement.network.ModNetworking;
import com.playerattributemanagement.common.network.AttributeSnapshot;
import com.playerattributemanagement.network.payload.SyncPlayerAttributesPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod.EventBusSubscriber(modid = Playerattributemanagement.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerAttributeDataHooks {
    private PlayerAttributeDataHooks() {}

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer newPlayer) || !(event.getOriginal() instanceof ServerPlayer original)) {
            return;
        }

        PlayerAttributeStore oldStore = readStore(original);
        PlayerAttributeStore newStore = new PlayerAttributeStore();
        newStore.copyFrom(oldStore);
        saveStore(newPlayer, newStore);
        PlayerAttributeApplier.applyAndClean(newPlayer, newStore);
        syncToClient(newPlayer, newStore);
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            refreshAndSync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            refreshAndSync(serverPlayer);
        }
    }

    public static void handleOpenRequest(ServerPlayer player) {
        if (!com.playerattributemanagement.config.AttributePanelConfig.get().isPanelEnabled()) {
            return;
        }

        refreshAndSync(player);
    }

    public static void handleClientSync(SyncPlayerAttributesPayload payload) {
        if (FMLEnvironment.dist.isClient()) {
            com.playerattributemanagement.client.PlayerAttributeClientBridge.acceptSnapshot(payload);
        }
    }

    public static void refreshAndSync(ServerPlayer player) {
        PlayerAttributeStore store = readStore(player);
        PlayerAttributeApplier.applyAndClean(player, store);
        saveStore(player, store);
        syncToClient(player, store);
    }

    public static void syncToClient(ServerPlayer player, PlayerAttributeStore store) {
        ModNetworking.sendToPlayer(player, buildPayload(player, store));
    }

    public static PlayerAttributeStore readStore(ServerPlayer player) {
        PlayerAttributeStore store = new PlayerAttributeStore();
        store.loadFrom(player.getPersistentData());
        return store;
    }

    public static void saveStore(ServerPlayer player, PlayerAttributeStore store) {
        store.saveTo(player.getPersistentData());
    }

    private static SyncPlayerAttributesPayload buildPayload(ServerPlayer player, PlayerAttributeStore store) {
        List<AttributeSnapshot> snapshots = new ArrayList<>();
        PlayerAttributeKeys.visibleAttributeIds().forEach(id -> {
            Attribute attribute = AttributeResolver.resolveAttribute(id, null);
            if (attribute == null) {
                return;
            }
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) {
                return;
            }

            double baseValue = instance.getBaseValue();
            double extraValue = store.getExtra(id);
            double totalValue = instance.getValue();
            String customName = com.playerattributemanagement.config.AttributePanelConfig.get().getCustomDisplayName(id);
            snapshots.add(new AttributeSnapshot(id.toString(), baseValue, extraValue, totalValue, customName));
        });

        return new SyncPlayerAttributesPayload(snapshots);
    }
}
