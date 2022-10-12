package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;

public interface JumpStatus extends AutoSyncedComponent, PlayerComponent<JumpStatus> {
	boolean isProhibited();
	void setProhibited(boolean prohibited);
}
