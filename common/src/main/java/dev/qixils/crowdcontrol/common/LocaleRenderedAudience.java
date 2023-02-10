package dev.qixils.crowdcontrol.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.renderer.ComponentRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class LocaleRenderedAudience implements RenderedAudience<Locale> {
	private final @NotNull Audience audience;
	private final @NotNull ComponentRenderer<Locale> renderer;

	@Override
	public @NotNull Locale context() {
		return audience.getOrDefault(Identity.LOCALE, Locale.getDefault());
	}
};
