package dev.qixils.crowdcontrol.plugin.paper.permissions;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;

public interface PermissionUtil {
	boolean hasPermission(Permissible permissible, Permission permission);
}
