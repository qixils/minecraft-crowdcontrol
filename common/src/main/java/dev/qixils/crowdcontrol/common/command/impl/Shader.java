package dev.qixils.crowdcontrol.common.command.impl;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.Versioned;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
public enum Shader implements Versioned {
	// vanilla effects
	BLUR(null),
	SPIDER(null),
	INVERT(null),
	// custom effects
	BITS(Versions.UPDATE_462),
	BLOBS2(Versions.UPDATE_462),
	BLUEPRINT(Versions.UPDATE_462),
	DOUBLE_VISION(Versions.UPDATE_462),
	FISHEYE(Versions.UPDATE_462),
	FLIP(Versions.UPDATE_462),
	MYOPIA(Versions.UPDATE_462),
	NTSC(Versions.UPDATE_462),
	PENCIL(Versions.UPDATE_462),
	SOBEL(Versions.UPDATE_462),
	SONAR(Versions.UPDATE_462),
	WOBBLE(Versions.UPDATE_462),
	;

	/**
	 * The newest version any bundled post effect was introduced in.
	 */
	public static final @NotNull SemVer NEWEST_BUNDLED = Arrays.stream(values())
		.map(shader -> shader.bundledIn)
		.filter(Objects::nonNull)
		.max(SemVer::compareTo)
		.orElse(SemVer.ZERO);

	/**
	 * For custom effects, the mod version that first added this shader, otherwise null.
	 */
	@Nullable
	private final SemVer bundledIn;
	@Nullable
	private final String customEffectId;

	Shader(@Nullable SemVer bundledIn, @Nullable String customEffectId) {
		this.bundledIn = bundledIn;
		this.customEffectId = customEffectId;
	}

	Shader(@Nullable SemVer bundledIn) {
		this(bundledIn, null);
	}

	/**
	 * Whether this post effect is custom.
	 */
	public boolean bundled() {
		return bundledIn != null;
	}

	/**
	 * For custom effects, the mod version that first added this shader, otherwise zero.
	 */
	@Override
	public @NotNull SemVer addedIn() {
		return bundledIn == null ? SemVer.ZERO : bundledIn;
	}

	@NotNull
	public String getPath() {
		return name().toLowerCase(Locale.ENGLISH);
	}

	@NotNull
	public Key getIdentifier() {
		return Key.key(bundled() ? Plugin.NAMESPACE : Key.MINECRAFT_NAMESPACE, getPath());
	}

	@NotNull
	public String getEffectId() {
		return "shader_" + Objects.requireNonNullElseGet(customEffectId, this::getPath);
	}

	// major milestones for new shaders
	// initializes before the enum
	private static final class Versions {
		private static final SemVer UPDATE_462 = new SemVer(4, 6, 2);
	}
}
