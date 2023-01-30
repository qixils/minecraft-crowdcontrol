package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import dev.qixils.crowdcontrol.common.util.SemVer;
import dev.qixils.crowdcontrol.common.util.Versioned;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

public interface MovementStatus extends AutoSyncedComponent, PlayerComponent<MovementStatus> {
	@NotNull
	Value get(@NotNull Type type);
	void set(@NotNull Type type, @NotNull Value value);
	void rawSet(@NotNull Type type, @NotNull Value value);
	void sync();

	@RequiredArgsConstructor
	@Getter
	@Accessors(fluent = true)
	enum Type implements Versioned {
		JUMP(false, new SemVer(3, 3, 0)),
		WALK(true, new SemVer(3, 3, 0)),
		LOOK(true, new SemVer(3, 3, 0)),
		;

		private final boolean canInvert;
		private final SemVer addedIn;
	}

	@RequiredArgsConstructor
	@Getter
	@Accessors(fluent = true)
	enum Value implements Versioned {
		ALLOWED(new SemVer(3, 3, 0)),
		PARTIAL(new SemVer(3, 3, 0)), // not supported by all types
		INVERTED(new SemVer(3, 3, 0)), // not supported by all types
		DENIED(new SemVer(3, 3, 0)),
		;

		private final SemVer addedIn;
	}
}
