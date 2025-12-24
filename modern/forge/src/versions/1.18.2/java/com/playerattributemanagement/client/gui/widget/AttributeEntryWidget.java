package com.playerattributemanagement.client.gui.widget;

import com.playerattributemanagement.text.TextCompat;
import com.playerattributemanagement.attribute.AttributeResolver;
import com.playerattributemanagement.common.network.AttributeSnapshot;
import com.playerattributemanagement.common.text.DisplayNameResolver;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class AttributeEntryWidget extends ObjectSelectionList.Entry<AttributeEntryWidget> {
    private final AttributeSnapshot snapshot;
    private final ResourceLocation attributeId;
    private final Component displayName;

    public AttributeEntryWidget(AttributeSnapshot snapshot) {
        this.snapshot = snapshot;
        this.attributeId = parseId(snapshot.id());
        this.displayName = resolveName(this.attributeId, snapshot.id(), snapshot.customName());
    }

    private static Component resolveName(ResourceLocation id, String rawId, String customName) {
        DisplayNameResolver.ResolvedName resolved = DisplayNameResolver.resolve(
            id,
            customName,
            attrId -> {
                if (attrId == null) {
                    return null;
                }
                Attribute attribute = AttributeResolver.resolveAttribute(attrId, null);
                return attribute != null ? attribute.getDescriptionId() : null;
            },
            ignored -> rawId
        );
        return resolved.translatable()
            ? TextCompat.translatable(resolved.value())
            : TextCompat.literal(resolved.value());
    }

    public AttributeSnapshot snapshot() {
        return this.snapshot;
    }

    @Override
    public Component getNarration() {
        return TextCompat.translatable("narration.playerattributemanagement.attribute", this.displayName);
    }

    @Override
    public void render(
        PoseStack poseStack,
        int entryIdx,
        int top,
        int left,
        int entryWidth,
        int entryHeight,
        int mouseX,
        int mouseY,
        boolean isMouseOver,
        float partialTick
    ) {
        Font font = Minecraft.getInstance().font;
        font.draw(poseStack, this.displayName, left + 6, top + 4, 0xFFFFFF);
        String totalString = formatNumber(this.snapshot.totalValue());
        font.draw(poseStack, totalString, left + entryWidth - font.width(totalString) - 6, top + 4, 0xA0A0A0);
    }

    public List<Component> tooltip() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(TextCompat.literal("Total: " + formatNumber(this.snapshot.totalValue())));
        tooltip.add(TextCompat.literal("Base: " + formatNumber(this.snapshot.baseValue())));
        tooltip.add(TextCompat.literal("Bonus: " + formatNumber(this.snapshot.extraValue())));
        String source = resolveSourceLabel(this.attributeId);
        if (source != null) {
            tooltip.add(TextCompat.literal("Source: " + source));
        }
        return tooltip;
    }

    private static String formatNumber(double value) {
        return String.format("%.4f", value);
    }

    private static String resolveSourceLabel(ResourceLocation id) {
        if (id == null) {
            return null;
        }
        String namespace = id.getNamespace();
        return "minecraft".equals(namespace) ? null : namespace;
    }

    private static ResourceLocation parseId(String raw) {
        ResourceLocation parsed = ResourceLocation.tryParse(raw);
        if (parsed != null) {
            return parsed;
        }
        return new ResourceLocation("minecraft", raw);
    }
}
