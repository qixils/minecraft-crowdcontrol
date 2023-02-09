package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A more sensible abstract implementation of a component renderer.
 *
 * @param <C> the context type
 */
@SuppressWarnings("NonExtendableApiUsage")
abstract class AbstractComponentRenderer<C> extends net.kyori.adventure.text.renderer.AbstractComponentRenderer<C> {
	private static final Set<Style.Merge> MERGES = Style.Merge.merges(Style.Merge.COLOR, Style.Merge.DECORATIONS, Style.Merge.INSERTION, Style.Merge.FONT);

	@Override
	protected @NotNull Component renderBlockNbt(final @NotNull BlockNBTComponent component, final @NotNull C context) {
		final BlockNBTComponent.Builder builder = this.nbt(context, Component.blockNBT(), component)
				.pos(component.pos());
		return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
	}

	@Override
	protected @NotNull Component renderEntityNbt(final @NotNull EntityNBTComponent component, final @NotNull C context) {
		final EntityNBTComponent.Builder builder = this.nbt(context, Component.entityNBT(), component)
				.selector(component.selector());
		return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
	}

	@Override
	protected @NotNull Component renderStorageNbt(final @NotNull StorageNBTComponent component, final @NotNull C context) {
		final StorageNBTComponent.Builder builder = this.nbt(context, Component.storageNBT(), component)
				.storage(component.storage());
		return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
	}

	protected <O extends NBTComponent<O, B>, B extends NBTComponentBuilder<O, B>> B nbt(final @NotNull C context, final B builder, final O oldComponent) {
		builder
				.nbtPath(oldComponent.nbtPath())
				.interpret(oldComponent.interpret());
		final @Nullable Component separator = oldComponent.separator();
		if (separator != null) {
			builder.separator(this.render(separator, context));
		}
		return builder;
	}

	@Override
	protected @NotNull Component renderKeybind(final @NotNull KeybindComponent component, final @NotNull C context) {
		final KeybindComponent.Builder builder = Component.keybind().keybind(component.keybind());
		return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
	}

	@Override
	@SuppressWarnings("deprecation")
	protected @NotNull Component renderScore(final @NotNull ScoreComponent component, final @NotNull C context) {
		final ScoreComponent.Builder builder = Component.score()
				.name(component.name())
				.objective(component.objective())
				.value(component.value());
		return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
	}

	@Override
	protected @NotNull Component renderSelector(final @NotNull SelectorComponent component, final @NotNull C context) {
		final SelectorComponent.Builder builder = Component.selector().pattern(component.pattern());
		return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
	}

	@Override
	protected @NotNull Component renderText(final @NotNull TextComponent component, final @NotNull C context) {
		final TextComponent.Builder builder = Component.text().content(component.content());
		return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
	}

	@Override
	protected @NotNull Component renderTranslatable(final @NotNull TranslatableComponent component, final @NotNull C context) {
		final TranslatableComponent.Builder builder = Component.translatable().key(component.key());
		List<Component> ogArgs = component.args();
		List<Component> newArgs = new ArrayList<>(ogArgs.size());
		for (Component arg : ogArgs)
			newArgs.add(this.render(arg, context));
		builder.args(newArgs);
		return this.mergeStyleAndOptionallyDeepRender(component, builder, context);
	}

	protected <O extends BuildableComponent<O, B>, B extends ComponentBuilder<O, B>> O mergeStyleAndOptionallyDeepRender(final Component component, final B builder, final C context) {
		this.mergeStyle(component, builder, context);
		return this.optionallyRenderChildrenAppendAndBuild(component.children(), builder, context);
	}

	protected <O extends BuildableComponent<O, B>, B extends ComponentBuilder<O, B>> O optionallyRenderChildrenAppendAndBuild(final List<Component> children, final B builder, final C context) {
		if (!children.isEmpty()) {
			children.forEach(child -> builder.append(this.render(child, context)));
		}
		return builder.build();
	}

	protected <B extends ComponentBuilder<?, ?>> void mergeStyle(final Component component, final B builder, final C context) {
		builder.mergeStyle(component, MERGES);
		builder.clickEvent(component.clickEvent());
		final @Nullable HoverEvent<?> hoverEvent = component.hoverEvent();
		if (hoverEvent != null) {
			builder.hoverEvent(hoverEvent.withRenderedValue(this, context));
		}
	}
}
