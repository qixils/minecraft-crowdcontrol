package dev.qixils.crowdcontrol.common.components;

import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.Versioned;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum MovementStatusValue implements Versioned {
	// 20 char limit
	ALLOWED(new SemVer("3.6.1")),
	PARTIAL(new SemVer("3.6.1")), // not supported by all types
	INVERTED(new SemVer("3.6.1")), // not supported by all types
	DENIED(new SemVer("3.6.1")),
	;

	private final SemVer addedIn;
}
