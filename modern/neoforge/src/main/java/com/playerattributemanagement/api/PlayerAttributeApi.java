package com.playerattributemanagement.api;

import com.playerattributemanagement.attribute.AttributeIdMapper;
import com.playerattributemanagement.attribute.PlayerAttributeKeys;
import com.playerattributemanagement.attribute.PlayerAttributeStore;
import com.playerattributemanagement.data.PlayerAttributeDataHooks;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * 简单的对外 API，方便其他模组直接调整玩家属性附加值。
 */
public final class PlayerAttributeApi {
    private PlayerAttributeApi() {}

    /**
     * 判断某属性是否受当前模组托管。
     */
    public static boolean isManagedAttribute(ResourceLocation id) {
        ResourceLocation canonical = AttributeIdMapper.normalize(id);
        return PlayerAttributeKeys.isManaged(canonical);
    }

    /**
     * 为指定属性设置绝对附加值，返回最新值。
     */
    public static double setExtra(ServerPlayer player, ResourceLocation id, double value) {
        ResourceLocation canonical = normalizeAndEnsure(id);
        PlayerAttributeStore store = PlayerAttributeDataHooks.readStore(player);
        store.setExtra(canonical, value);
        PlayerAttributeDataHooks.saveStore(player, store);
        PlayerAttributeDataHooks.refreshAndSync(player);
        return value;
    }

    /**
     * 在现有附加值的基础上增加 delta，返回递增后的值。
     */
    public static double addExtra(ServerPlayer player, ResourceLocation id, double delta) {
        ResourceLocation canonical = normalizeAndEnsure(id);
        PlayerAttributeStore store = PlayerAttributeDataHooks.readStore(player);
        double newValue = store.getExtra(canonical) + delta;
        store.setExtra(canonical, newValue);
        PlayerAttributeDataHooks.saveStore(player, store);
        PlayerAttributeDataHooks.refreshAndSync(player);
        return newValue;
    }

    /**
     * 清除指定属性的附加值。
     */
    public static void resetExtra(ServerPlayer player, ResourceLocation id) {
        ResourceLocation canonical = normalizeAndEnsure(id);
        PlayerAttributeStore store = PlayerAttributeDataHooks.readStore(player);
        store.setExtra(canonical, 0.0);
        PlayerAttributeDataHooks.saveStore(player, store);
        PlayerAttributeDataHooks.refreshAndSync(player);
    }

    /**
     * 读取指定属性当前的附加值（若未设置则返回 0）。
     */
    public static double getExtra(ServerPlayer player, ResourceLocation id) {
        ResourceLocation canonical = normalizeAndEnsure(id);
        PlayerAttributeStore store = PlayerAttributeDataHooks.readStore(player);
        return store.getExtra(canonical);
    }

    /**
     * 返回玩家所有附加值的快照。
     */
    public static Map<ResourceLocation, Double> getAllExtras(ServerPlayer player) {
        PlayerAttributeStore store = PlayerAttributeDataHooks.readStore(player);
        return store.getAllExtras();
    }

    private static ResourceLocation normalizeAndEnsure(ResourceLocation id) {
        ResourceLocation canonical = AttributeIdMapper.normalize(id);
        if (!PlayerAttributeKeys.isManaged(canonical)) {
            throw new IllegalArgumentException("属性 " + id + " 不受 playerattributemanagement 管理");
        }
        return canonical;
    }
}
