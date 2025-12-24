package com.playerattributemanagement.client;

import com.playerattributemanagement.common.network.AttributeSnapshot;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.playerattributemanagement.util.Identifiers;

@Environment(EnvType.CLIENT)
public class PlayerAttributeScreen extends Screen {
	private static final DecimalFormat NUMBER = new DecimalFormat("0.00");
	private AttributeListWidget list;

	public PlayerAttributeScreen() {
		super(Text.translatable("screen.playerattributemanagement.title"));
	}

	@Override
	protected void init() {
		int top = 36;
		int bottom = this.height - 45;
		this.list = new AttributeListWidget(this.client, this.width, this.height, top, bottom, 24);
		this.list.setEntries(ClientAttributeCache.get());
		this.addSelectableChild(list);

		this.addDrawableChild(
			ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
				.dimensions(this.width / 2 - 50, this.height - 30, 100, 20)
				.build()
		);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
		this.list.render(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);

		AttributeEntry hovered = this.list.entryAt(mouseX, mouseY);
		if (hovered != null) {
			hovered.renderTooltip(context, mouseX, mouseY);
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	public void close() {
		MinecraftClient.getInstance().setScreen(null);
	}

	private class AttributeListWidget extends AlwaysSelectedEntryListWidget<AttributeEntry> {
		public AttributeListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
			super(client, width, height, top, bottom, itemHeight);
		}

		public void setEntries(List<AttributeSnapshot> entries) {
			this.clearEntries();
			for (AttributeSnapshot entry : entries) {
				this.addEntry(new AttributeEntry(this, entry));
			}
		}

		@Override
		protected int getScrollbarPositionX() {
			return this.width - 6;
		}

		@Override
		public int getRowWidth() {
			return this.width - 12;
		}

		public AttributeEntry entryAt(double mouseX, double mouseY) {
			return this.getEntryAtPosition(mouseX, mouseY);
		}
	}

	private class AttributeEntry extends AlwaysSelectedEntryListWidget.Entry<AttributeEntry> {
		private final Identifier id;
		private final double base;
		private final double extra;
		private final double total;
		private final String customName;

		AttributeEntry(AttributeListWidget parent, AttributeSnapshot data) {
			this.id = parseId(data.id());
			this.base = data.baseValue();
			this.extra = data.extraValue();
			this.total = data.totalValue();
			this.customName = data.customName();
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			int idColor = 0xE0E0E0;
			int totalColor = 0xFFFFFF;

			Text display = AttributeNameResolver.resolve(this.id, this.customName);
			context.drawTextWithShadow(PlayerAttributeScreen.this.textRenderer, display, x + 6, y + 5, idColor);

			String totalStr = NUMBER.format(this.total);
			context.drawTextWithShadow(PlayerAttributeScreen.this.textRenderer, totalStr, x + entryWidth - PlayerAttributeScreen.this.textRenderer.getWidth(totalStr) - 8, y + 5, totalColor);
		}

		void renderTooltip(DrawContext context, int mouseX, int mouseY) {
			List<Text> tooltip = new ArrayList<>();
			tooltip.add(Text.translatable("tooltip.playerattributemanagement.total", NUMBER.format(this.total)));
			tooltip.add(Text.translatable("tooltip.playerattributemanagement.base", NUMBER.format(this.base)));
			tooltip.add(Text.translatable("tooltip.playerattributemanagement.extra", NUMBER.format(this.extra)));
			String namespace = this.id.getNamespace();
			if (!"minecraft".equals(namespace)) {
				tooltip.add(Text.translatable("tooltip.playerattributemanagement.source", namespace));
			}
			context.drawTooltip(PlayerAttributeScreen.this.textRenderer, tooltip, mouseX, mouseY);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return false;
		}

		@Override
		public Text getNarration() {
			return AttributeNameResolver.resolve(this.id, this.customName);
		}
	}

	private static Identifier parseId(String raw) {
		Identifier parsed = Identifier.tryParse(raw);
		if (parsed != null) {
			return parsed;
		}
		return Identifiers.fromNamespacePath("minecraft", raw);
	}
}
