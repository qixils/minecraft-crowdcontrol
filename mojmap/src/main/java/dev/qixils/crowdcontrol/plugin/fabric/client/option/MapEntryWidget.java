package dev.qixils.crowdcontrol.plugin.fabric.client.option;

import com.google.common.collect.ImmutableList;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.TooltipButtonWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MapEntryWidget<K, V> extends AbstractWidget implements ContainerEventHandler {
	private final TooltipButtonWidget removeButton;
	private final AbstractWidget keyWidget;
	private final AbstractWidget valueWidget;

	private final MapOption<K, V> mapOption;
	private final MapOptionEntry<K, V> mapOptionEntry;

	private final String optionNameString;

	private GuiEventListener focused;
	private boolean dragging;

	public MapEntryWidget(YACLScreen screen, MapOptionEntry<K, V> mapOptionEntry, AbstractWidget keyWidget, AbstractWidget valueWidget) {
		super(Dimension.ofInt(keyWidget.getDimension().x(), keyWidget.getDimension().y(), keyWidget.getDimension().width(), Math.max(Math.min(keyWidget.getDimension().height(), 20), Math.min(valueWidget.getDimension().height(), 20))));
		this.mapOptionEntry = mapOptionEntry;
		this.mapOption = mapOptionEntry.parentGroup();
		this.optionNameString = mapOptionEntry.name().getString().toLowerCase();

		this.keyWidget = keyWidget;
		this.valueWidget = valueWidget;

		Dimension<Integer> dim = getDimension();
		int contentWidth = dim.width() - 24; // 24 for button and padding
		int halfWidth = contentWidth / 2;

		this.keyWidget.setDimension(Dimension.ofInt(dim.x() + 24, dim.y(), halfWidth - 2, dim.height()));
		this.valueWidget.setDimension(Dimension.ofInt(dim.x() + 24 + halfWidth + 2, dim.y(), halfWidth - 2, dim.height()));

		this.removeButton = new TooltipButtonWidget(screen, dim.x(), dim.y(), 20, 20, Component.literal("\u274c"), Component.translatable("yacl.list.remove"), btn -> {
			mapOption.removeEntry(mapOptionEntry);
			updateButtonStates();
			if (mapOption instanceof MapOptionImpl) {
				MapOptionImpl.attemptRefresh(screen);
			}
		});

		updateButtonStates();
	}

	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float a) {
		updateButtonStates();

		removeButton.setY(getDimension().y());
		keyWidget.setDimension(keyWidget.getDimension().withY(getDimension().y()));
		valueWidget.setDimension(valueWidget.getDimension().withY(getDimension().y()));

		removeButton.render(graphics, mouseX, mouseY, a);
		keyWidget.render(graphics, mouseX, mouseY, a);
		valueWidget.render(graphics, mouseX, mouseY, a);
	}

	protected void updateButtonStates() {
		removeButton.active = mapOption.available() && mapOption.numberOfEntries() > mapOption.minimumNumberOfEntries();
	}

	@Override
	public void unfocus() {
		keyWidget.unfocus();
		valueWidget.unfocus();
	}

	@Override
	public void updateNarration(NarrationElementOutput builder) {
		keyWidget.updateNarration(builder);
		valueWidget.updateNarration(builder);
	}

	@Override
	public boolean matchesSearch(String query) {
		return optionNameString.contains(query.toLowerCase());
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return ImmutableList.of(removeButton, keyWidget, valueWidget);
	}

	@Override
	public boolean isDragging() {
		return dragging;
	}

	@Override
	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}

	@Nullable
	@Override
	public GuiEventListener getFocused() {
		return focused;
	}

	@Override
	public void setFocused(@Nullable GuiEventListener focused) {
		this.focused = focused;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return ContainerEventHandler.super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.setDragging(false);
		return ContainerEventHandler.super.mouseReleased(mouseX, mouseY, button);
	}


	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
		return ContainerEventHandler.super.mouseDragged(mouseX, mouseY, button, dx, dy);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return ContainerEventHandler.super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return ContainerEventHandler.super.keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		return ContainerEventHandler.super.charTyped(chr, modifiers);
	}
}
