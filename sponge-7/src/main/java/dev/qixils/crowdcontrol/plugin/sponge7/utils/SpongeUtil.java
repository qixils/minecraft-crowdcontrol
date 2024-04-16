package dev.qixils.crowdcontrol.plugin.sponge7.utils;

import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Tristate;

public class SpongeUtil {
	private SpongeUtil() {}

	public static boolean hasPermission(Subject subject, PermissionWrapper permission) {
		Tristate hasPerm = subject.getPermissionValue(subject.getActiveContexts(), permission.getNode());
		if (hasPerm != Tristate.UNDEFINED) return hasPerm.asBoolean();

		switch (permission.getDefaultPermission()) {
			case ALL:
				return true;
			case NONE:
				return false;
			case OP:
				return false; // TODO
			default:
				throw new IllegalStateException("Unknown default permission " + permission.getDefaultPermission());
		}
	}
}
