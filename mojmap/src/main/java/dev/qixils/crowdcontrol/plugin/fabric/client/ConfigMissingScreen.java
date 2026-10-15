package dev.qixils.crowdcontrol.plugin.fabric.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ConfigMissingScreen extends Screen {
	private final Screen parent;

	public ConfigMissingScreen(Screen parent) {
		super(Component.empty());
		this.parent = parent;
	}

	@Override
	protected void init() {
		Button buttonWidget = Button.builder(
			Component.translatable("config.crowdcontrol.missing_back"),
			(btn) -> onClose()
		).bounds(20, 20, 120, 20).build();
		// x, y, width, height
		// It's recommended to use the fixed height of 20 to prevent rendering issues with the button
		// textures.

		// Register the button widget.
		this.addRenderableWidget(buttonWidget);

	}

	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float a) {
		super.render(graphics, mouseX, mouseY, a);

		// Minecraft doesn't have a "label" widget, so we'll have to draw our own text.
		// textRenderer, text, x, y, maxLineLength, color
		graphics.drawWordWrap(this.font, Component.translatable("config.crowdcontrol.missing"), 20, 50, 200, 0xFFFFFFFF);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}
}
