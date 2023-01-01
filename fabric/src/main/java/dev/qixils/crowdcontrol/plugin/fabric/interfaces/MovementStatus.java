package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
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
	enum Type {
		JUMP(false),
		WALK(true),
		LOOK(true),
		;

		@Getter @Accessors(fluent = true)
		private final boolean canInvert;
	}

	enum Value {
		ALLOWED,
		PARTIAL, // not supported by all types
		INVERTED, // not supported by all types
		DENIED
	}
}
