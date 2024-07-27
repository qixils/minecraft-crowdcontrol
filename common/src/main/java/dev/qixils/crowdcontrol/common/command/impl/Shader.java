package dev.qixils.crowdcontrol.common.command.impl;

import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.Versioned;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum Shader implements Versioned {
	BUMPY(new SemVer("3.3.0")),
	GREEN(new SemVer("3.3.0")),
	NTSC(new SemVer("3.3.0")),
	DESATURATE(new SemVer("3.3.0")),
	FLIP(new SemVer("3.3.0")),
	INVERT(new SemVer("3.3.0")),
	BLOBS2(new SemVer("3.3.0")),
	PENCIL(new SemVer("3.3.0")),
	SOBEL(new SemVer("3.3.0")),
	CC_WOBBLE(new SemVer("3.3.0")),
	BITS(new SemVer("3.3.0")),
	SPIDER(new SemVer("3.3.0")),
	PHOSPHOR(new SemVer("3.3.0")),
	;

	@NotNull
	private final SemVer addedIn;

	@NotNull
	public String getShaderId() {
		return name().toLowerCase(Locale.ENGLISH);
	}

	@NotNull
	public String getEffectId() {
		return "shader_" + getShaderId().replaceFirst("^cc_", "");
	}
}
