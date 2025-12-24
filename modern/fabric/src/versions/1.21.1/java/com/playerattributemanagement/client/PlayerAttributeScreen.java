package com.playerattributemanagement.client;

import com.playerattributemanagement.common.network.AttributeSnapshot;
import com.playerattributemanagement.util.Identifiers;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PlayerAttributeScreen extends Screen {
	private static final DecimalFormat NUMBER = new DecimalFormat("0.00");
	private AttributeList list;

	public PlayerAttributeScreen() {
		super(Text.translatable("screen.playerattributemanagement.title"));
	}

	@Override
	protected void init() {
		int top = 36;
		int bottom = this.height - 45;
		this.list = new AttributeList(this.width, top, bottom, 24);
		this.list.setEntries(ClientAttributeCache.get());

		this.addDrawableChild(
			ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
				.dimensions(this.width / 2 - 50, this.height - 30, 100, 20)
				.build()
		);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderDimBackground(context);
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

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.list.mouseScrolled(mouseX, mouseY, verticalAmount)) {
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderDimBackground(context);
	}

	private void renderDimBackground(DrawContext context) {
		context.fill(0, 0, this.width, this.height, 0xAA101010);
	}

	private class AttributeList {
		private final int left = 16;
		private final int right = PlayerAttributeScreen.this.width - 16;
		private final int top;
		private final int bottom;
		private final int entryHeight;
		private final List<AttributeEntry> entries = new ArrayList<>();
		private double scroll;

		AttributeList(int screenWidth, int top, int bottom, int entryHeight) {
			this.top = top;
			this.bottom = bottom;
			this.entryHeight = entryHeight;
		}

		void setEntries(List<AttributeSnapshot> data) {
			this.entries.clear();
			for (AttributeSnapshot entry : data) {
				this.entries.add(new AttributeEntry(entry));
			}
			this.scroll = 0;
		}

		void render(DrawContext context, int mouseX, int mouseY, float delta) {
			int panelLeft = this.left;
			int panelRight = this.right;
			context.fill(panelLeft - 6, this.top - 6, panelRight + 6, this.bottom + 6, 0xB0000000);
			context.drawBorder(panelLeft - 6, this.top - 6, panelRight - panelLeft + 12, this.bottom - this.top + 12, 0x55FFFFFF);

			context.enableScissor(panelLeft, this.top, panelRight, this.bottom);
			int visibleHeight = this.bottom - this.top;
			int startIndex = Math.max(0, (int)(this.scroll / this.entryHeight));
			int endIndex = Math.min(this.entries.size(), startIndex + visibleHeight / this.entryHeight + 2);
			for (int i = startIndex; i < endIndex; i++) {
				int y = this.top - (int)this.scroll + i * this.entryHeight;
				boolean hovered = mouseX >= panelLeft && mouseX <= panelRight && mouseY >= y && mouseY <= y + this.entryHeight;
				this.entries.get(i).renderRow(context, panelLeft, y, panelRight - panelLeft, this.entryHeight, hovered);
			}
			context.disableScissor();

			if (this.getMaxScroll() > 0) {
				int scrollBarWidth = 4;
				int height = this.bottom - this.top;
				int barHeight = Math.max(14, (int)((float)height * height / (float)(this.entries.size() * this.entryHeight)));
				int barX = panelRight + 4;
				int barY = (int)(this.scroll * (height - barHeight) / this.getMaxScroll()) + this.top;
				context.fill(barX, this.top, barX + scrollBarWidth, this.bottom, 0x44000000);
				context.fill(barX, barY, barX + scrollBarWidth, barY + barHeight, 0xAAFFFFFF);
			}
		}

		boolean mouseScrolled(double mouseX, double mouseY, double amount) {
			if (!this.isWithin(mouseX, mouseY)) {
				return false;
			}
			this.scroll = MathHelper.clamp(this.scroll - amount * (double)this.entryHeight, 0.0, this.getMaxScroll());
			return true;
		}

		private boolean isWithin(double mouseX, double mouseY) {
			return mouseX >= this.left && mouseX <= this.right && mouseY >= this.top && mouseY <= this.bottom;
		}

		private double getMaxScroll() {
			return Math.max(0, this.entries.size() * this.entryHeight - (this.bottom - this.top));
		}

		AttributeEntry entryAt(double mouseX, double mouseY) {
			if (!this.isWithin(mouseX, mouseY)) {
				return null;
			}
			int relativeY = (int)(mouseY - this.top + this.scroll);
			int index = relativeY / this.entryHeight;
			if (index >= 0 && index < this.entries.size()) {
				return this.entries.get(index);
			}
			return null;
		}
	}

	private class AttributeEntry {
		private final Identifier id;
		private final double base;
		private final double extra;
		private final double total;
		private final String customName;

		AttributeEntry(AttributeSnapshot data) {
			this.id = parseId(data.id());
			this.base = data.baseValue();
			this.extra = data.extraValue();
			this.total = data.totalValue();
			this.customName = data.customName();
		}

		void renderRow(DrawContext context, int x, int y, int width, int height, boolean hovered) {
			int bg = hovered ? 0x44FFFFFF : 0x22000000;
			context.fill(x, y, x + width, y + height, bg);
			int idColor = 0xE0E0E0;
			int totalColor = 0xFFFFFF;

			Text display = AttributeNameResolver.resolve(this.id, this.customName);
			context.drawTextWithShadow(PlayerAttributeScreen.this.textRenderer, display, x + 6, y + 5, idColor);

			String totalStr = NUMBER.format(this.total);
			int textWidth = PlayerAttributeScreen.this.textRenderer.getWidth(totalStr);
			context.drawTextWithShadow(PlayerAttributeScreen.this.textRenderer, totalStr, x + width - textWidth - 6, y + 5, totalColor);
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
