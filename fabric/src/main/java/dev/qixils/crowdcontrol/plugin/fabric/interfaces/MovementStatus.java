package dev.qixils.crowdcontrol.plugin.fabric.interfaces;

import dev.qixils.crowdcontrol.common.components.MovementStatusType;
import dev.qixils.crowdcontrol.common.components.MovementStatusValue;
import org.jetbrains.annotations.NotNull;

public interface MovementStatus {

	@NotNull
	default MovementStatusValue cc$getMovementStatus(@NotNull MovementStatusType type) {
		return MovementStatusValue.ALLOWED;
	}

	default void cc$setMovementStatus(@NotNull MovementStatusType type, @NotNull MovementStatusValue value) {

	}

}
