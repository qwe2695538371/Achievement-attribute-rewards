package com.playerattributemanagement.client.gui;

import com.playerattributemanagement.text.TextCompat;
import com.playerattributemanagement.client.ClientAttributeCache;
import com.playerattributemanagement.client.gui.widget.AttributeEntryWidget;
import com.playerattributemanagement.common.network.AttributeSnapshot;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class PlayerAttributeScreen extends Screen {
    private AttributeListWidget listWidget;

    public PlayerAttributeScreen() {
        super(TextCompat.translatable("screen.playerattributemanagement.attribute_panel"));
    }

    @Override
    protected void init() {
        int top = 32;
        int bottom = this.height - 45;
        this.listWidget = new AttributeListWidget(this.minecraft, this.width, this.height, top, bottom, 24);
        this.addRenderableWidget(this.listWidget);
        this.addRenderableWidget(new Button(this.width / 2 - 50, this.height - 30, 100, 20,
            TextCompat.translatable("gui.done"), button -> this.onClose()));
        this.reloadData();
    }

    public void reloadData() {
        if (this.listWidget != null) {
            List<AttributeSnapshot> entries = ClientAttributeCache.get().orderedEntries();
            this.listWidget.reload(entries);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        super.render(poseStack, mouseX, mouseY, partialTick);
        if (this.listWidget != null) {
            AttributeEntryWidget hovered = this.listWidget.entryAt(mouseX, mouseY);
            if (hovered != null) {
                List<FormattedCharSequence> tooltip = hovered.tooltip().stream()
                    .map(Component::getVisualOrderText)
                    .toList();
                this.renderTooltip(poseStack, tooltip, mouseX, mouseY);
            }
        }
    }

    class AttributeListWidget extends ObjectSelectionList<AttributeEntryWidget> {
        AttributeListWidget(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
            super(minecraft, width, height, top, bottom, itemHeight);
        }

        void reload(List<AttributeSnapshot> entries) {
            this.clearEntries();
            entries.forEach(snapshot -> this.addEntry(new AttributeEntryWidget(snapshot)));
        }

        @Nullable
        AttributeEntryWidget entryAt(int mouseX, int mouseY) {
            return this.getEntryAtPosition(mouseX, mouseY);
        }

        protected int getScrollbarPosition() {
            return this.width - 6;
        }

        public int getRowWidth() {
            return this.width - 12;
        }
    }
}
