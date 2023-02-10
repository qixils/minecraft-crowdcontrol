package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.renderer.ComponentRenderer;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * An audience that has its messages rendered before being sent.
 */
@SuppressWarnings({"deprecation", "UnstableApiUsage"})
public interface RenderedAudience<C> extends ForwardingAudience.Single {

	@ApiStatus.OverrideOnly
	@NotNull ComponentRenderer<C> renderer();

	@ApiStatus.OverrideOnly
	@NotNull C context();

	@ApiStatus.NonExtendable
	default @NotNull Component render(final @NotNull ComponentLike component) {
		// I could further abstract this method out to a separate interface, but unless I want to make a dedicated
		// adventure extension library (which I might want to at some point but not today), it's not necessary here.
		return renderer().render(component.asComponent(), context());
	}

	@ApiStatus.NonExtendable
	default @NotNull List<Component> render(final @NotNull Collection<? extends ComponentLike> components) {
		return components.stream().map(this::render).collect(Collectors.toList());
	}

	@Override
	default void sendMessage(final @NotNull Component message) {
		ForwardingAudience.Single.super.sendMessage(render(message));
	}

	@Override
	default void sendMessage(final @NotNull Component message, final ChatType.@NotNull Bound boundChatType) {
		ForwardingAudience.Single.super.sendMessage(render(message), boundChatType);
	}

	@Override
	@Deprecated
	default void sendMessage(final @NotNull Identified source, final @NotNull Component message, final @NotNull MessageType type) {
		ForwardingAudience.Single.super.sendMessage(source, render(message), type);
	}

	@Override
	@Deprecated
	default void sendMessage(final @NotNull Identity source, final @NotNull Component message, final @NotNull MessageType type) {
		ForwardingAudience.Single.super.sendMessage(source, render(message), type);
	}

	@Override
	default void sendActionBar(@NotNull Component message) {
		ForwardingAudience.Single.super.sendActionBar(render(message));
	}

	@Override
	default void sendPlayerListHeader(@NotNull Component header) {
		ForwardingAudience.Single.super.sendPlayerListHeader(render(header));
	}

	@Override
	default void sendPlayerListFooter(@NotNull Component footer) {
		ForwardingAudience.Single.super.sendPlayerListFooter(render(footer));
	}

	@Override
	default void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
		ForwardingAudience.Single.super.sendPlayerListHeaderAndFooter(render(header), render(footer));
	}

	@Override
	default void showBossBar(@NotNull BossBar bar) {
		bar.name(render(bar.name()));
		ForwardingAudience.Single.super.showBossBar(bar);
	}

	@Override
	default void openBook(@NotNull Book book) {
		ForwardingAudience.Single.super.openBook(
				book.toBuilder()
						.title(render(book.title()))
						.author(render(book.author()))
						.pages(render(book.pages()))
						.build()
		);
	}

	@Override
	default void showTitle(@NotNull Title title) {
		ForwardingAudience.Single.super.showTitle(Title.title(
				render(title.title()),
				render(title.subtitle()),
				title.times()
		));
	}

	static @NotNull RenderedAudience<Locale> translated(Audience audience, ComponentRenderer<Locale> translator) {
		if (audience instanceof LocaleRenderedAudience)
			return (LocaleRenderedAudience) audience;
		return new LocaleRenderedAudience(audience, translator);
	}
}
