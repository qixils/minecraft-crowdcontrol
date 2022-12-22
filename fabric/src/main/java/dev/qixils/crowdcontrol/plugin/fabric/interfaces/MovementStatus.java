package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import org.jetbrains.annotations.NotNull;

// TODO: is resetting handled successfully for offline players?

public interface MovementStatus extends AutoSyncedComponent, PlayerComponent<MovementStatus> {
	boolean isProhibited(@NotNull Type type);
	void setProhibited(@NotNull Type type, boolean prohibited);

	enum Type {
		JUMP,
		WALK,
		LOOK,
	}
}
