package dev.qixils.crowdcontrol.plugin.neoforge.util;

import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import dev.qixils.crowdcontrol.plugin.fabric.utils.PermissionUtil;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.PermissionSetSupplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class NeoForgePermissionUtil extends PermissionUtil {
	private boolean check(PermissionSet permissionSet, PermissionWrapper permission) {
		return permissionSet.hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(permission.getPermissionLevel())));
	}

	@Override
	public boolean check(Entity entity, PermissionWrapper permission) {
		if (entity instanceof Player player)
			return check(player.permissions(), permission);
		return permission.getPermissionLevel() == 0;
	}

	@Override
	public boolean check(PermissionSetSupplier source, PermissionWrapper permission) {
		return check(source.permissions(), permission);
	}
}
