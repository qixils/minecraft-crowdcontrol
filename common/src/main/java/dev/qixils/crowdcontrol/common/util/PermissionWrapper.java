package dev.qixils.crowdcontrol.common.util;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@Builder
public class PermissionWrapper {
	private final @NotNull String node;
	private final @NotNull DefaultPermission defaultPermission;
	@Builder.Default
	private final @Nullable String description = null;

	public enum DefaultPermission {
		/**
		 * All players
		 */
		ALL,
		/**
		 * Opped players
		 */
		OP,
		/**
		 * No players
		 */
		NONE,
	}
}
