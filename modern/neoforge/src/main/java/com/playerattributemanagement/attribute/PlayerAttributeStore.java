package com.playerattributemanagement.attribute;

import com.playerattributemanagement.Playerattributemanagement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

/**
 * 玩家属性附加值的持久化存储。
 * <p>
 * 使用 {@link #saveTo(CompoundTag)} 和 {@link #loadFrom(CompoundTag)} 与玩家 {@code persistentData}
 * 交互，保证死亡 / 维度切换后仍能保留自定义属性调整。
 */
public class PlayerAttributeStore {
    private static final String ROOT_KEY = Playerattributemanagement.MODID;
    private static final String LIST_KEY = "attributeExtras";
    private static final String ATTR_ID_KEY = "attribute";
    private static final String EXTRA_VALUE_KEY = "extra";

    private final Map<ResourceLocation, Double> extras = new HashMap<>();

    public double getExtra(ResourceLocation id) {
        ResourceLocation canonical = AttributeIdMapper.normalize(id);
        return this.extras.getOrDefault(canonical, 0.0);
    }

    public Map<ResourceLocation, Double> getAllExtras() {
        return Collections.unmodifiableMap(this.extras);
    }

    public void setExtra(ResourceLocation id, double value) {
        ResourceLocation canonical = AttributeIdMapper.normalize(id);
        if (Math.abs(value) < 1.0E-6) {
            this.extras.remove(canonical);
        } else {
            this.extras.put(canonical, value);
        }
    }

    public void clear() {
        this.extras.clear();
    }

    public void copyFrom(PlayerAttributeStore other) {
        this.extras.clear();
        this.extras.putAll(other.extras);
    }

    public void loadFrom(CompoundTag playerPersistentData) {
        this.extras.clear();
        CompoundTag root = getOrCreateRoot(playerPersistentData);
        if (!NbtCompat.has(root, LIST_KEY, net.minecraft.nbt.Tag.TAG_LIST)) {
            return;
        }

        ListTag listTag = NbtCompat.getList(root, LIST_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND);
        listTag.forEach(entry -> {
            if (!(entry instanceof CompoundTag compound)) {
                return;
            }

            if (!NbtCompat.has(compound, ATTR_ID_KEY, net.minecraft.nbt.Tag.TAG_STRING)
                || !NbtCompat.has(compound, EXTRA_VALUE_KEY, net.minecraft.nbt.Tag.TAG_DOUBLE)) {
                return;
            }

            ResourceLocation rawId = ResourceLocation.tryParse(NbtCompat.getString(compound, ATTR_ID_KEY));
            if (rawId == null) {
                return;
            }
            ResourceLocation canonical = AttributeIdMapper.normalize(rawId);
            this.extras.put(canonical, NbtCompat.getDouble(compound, EXTRA_VALUE_KEY));
        });
    }

    public void saveTo(CompoundTag playerPersistentData) {
        CompoundTag root = getOrCreateRoot(playerPersistentData);
        ListTag listTag = new ListTag();
        this.extras.forEach((id, value) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString(ATTR_ID_KEY, id.toString());
            entry.putDouble(EXTRA_VALUE_KEY, value);
            listTag.add(entry);
        });
        root.put(LIST_KEY, listTag);
    }

    private static CompoundTag getOrCreateRoot(CompoundTag playerPersistentData) {
        if (!NbtCompat.has(playerPersistentData, ROOT_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            playerPersistentData.put(ROOT_KEY, new CompoundTag());
        }

        CompoundTag root = NbtCompat.getCompound(playerPersistentData, ROOT_KEY);
        return root != null ? root : new CompoundTag();
    }

    public void forEach(Consumer<Map.Entry<ResourceLocation, Double>> consumer) {
        this.extras.entrySet().forEach(consumer);
    }
}
