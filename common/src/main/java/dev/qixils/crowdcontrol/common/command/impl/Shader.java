package dev.qixils.crowdcontrol.common.command.impl;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.Versioned;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
public enum Shader implements Versioned {
	// TODO: incorporate some community shaders
	//  https://github.com/search?q=path%3A*.json+path%3A%2Fassets%5C%2F.%2B%5C%2Fpost_effect%2F&type=code&ref=advsearch
	CREEPER(null, "green"),
	BLUR(null),
	SPIDER(null),
	INVERT(null),
	;

	@Nullable
	private final SemVer addedIn;
	@Nullable
	private final String customEffectId;

	Shader(@Nullable SemVer addedIn, @Nullable String customEffectId) {
		this.addedIn = addedIn;
		this.customEffectId = customEffectId;
	}

	Shader(@Nullable SemVer addedIn) {
		this(addedIn, null);
	}

	@NotNull
	public Key getIdentifier() {
		return Key.key(addedIn == null ? Key.MINECRAFT_NAMESPACE : Plugin.NAMESPACE, name().toLowerCase(Locale.ENGLISH));
	}

	@NotNull
	public String getEffectId() {
		return "shader_" + Objects.requireNonNullElseGet(customEffectId, () -> name().toLowerCase(Locale.ENGLISH));
	}
}
