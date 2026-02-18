package dev.qixils.crowdcontrol.plugin.paper.permissions;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;

public class PaperPermissionUtil implements PermissionUtil {
	@Override
	public boolean hasPermission(Permissible permissible, Permission permission) {
		return switch (permissible.permissionValue(permission)) {
			case TRUE -> true;
			case FALSE -> false;
			default -> permission.getDefault().getValue(permissible.isOp());
		};
	}
}
