package dev.qixils.crowdcontrol.common.components;

import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.Versioned;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum MovementStatusType implements Versioned {
	// 20 char limit
	JUMP(false, new SemVer("3.6.1")),
	WALK(true, new SemVer("3.6.1")),
	LOOK(true, new SemVer("3.6.1")),
	;

	private final boolean canInvert;
	private final SemVer addedIn;
}
