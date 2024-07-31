package dev.qixils.crowdcontrol.common.packets.util;

import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.Versioned;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum LanguageState implements Versioned {
	RESET(new SemVer("3.6.1")),
	RANDOM(new SemVer("3.6.1")),
	;

	private final @NotNull SemVer addedIn;
}
