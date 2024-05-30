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

	public int getPermissionLevel() {
		return defaultPermission.getPermissionLevel();
	}

	public enum DefaultPermission {
		/**
		 * All players
		 */
		ALL(0),
		/**
		 * Opped players
		 */
		OP(4),
		/**
		 * No players
		 */
		NONE(-1),
		;

		private final int permissionLevel;

		DefaultPermission(int permissionLevel) {
			this.permissionLevel = permissionLevel;
		}

		public int getPermissionLevel() {
			return permissionLevel;
		}
	}
}
