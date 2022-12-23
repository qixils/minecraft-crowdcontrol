package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

// TODO: is resetting handled successfully for offline players?

public interface MovementStatus extends AutoSyncedComponent, PlayerComponent<MovementStatus> {
	boolean isProhibited(@NotNull Type type);
	void setProhibited(@NotNull Type type, boolean prohibited);

	boolean isInverted(@NotNull Type type);
	void setInverted(@NotNull Type type, boolean inverted);

	@RequiredArgsConstructor
	enum Type {
		JUMP(false),
		WALK(true),
		LOOK(true),
		;

		@Getter @Accessors(fluent = true)
		private final boolean canInvert;
	}
}
