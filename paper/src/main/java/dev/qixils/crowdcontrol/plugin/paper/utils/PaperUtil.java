package dev.qixils.crowdcontrol.plugin.paper.utils;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class PaperUtil {
	private PaperUtil() {}

	public static PermissionDefault toPaper(PermissionWrapper.DefaultPermission defaultPermission) {
		return switch (defaultPermission) {
			case ALL -> PermissionDefault.TRUE;
			case OP -> PermissionDefault.OP;
			case NONE -> PermissionDefault.FALSE;
        };
	}

	public static Permission toPaper(PermissionWrapper permission) {
		return new Permission(permission.getNode(), permission.getDescription(), toPaper(permission.getDefaultPermission()));
	}

	public static final Permission USE_PERMISSION = toPaper(Plugin.USE_PERMISSION);
	public static final Permission ADMIN_PERMISSION = toPaper(Plugin.ADMIN_PERMISSION);
}
