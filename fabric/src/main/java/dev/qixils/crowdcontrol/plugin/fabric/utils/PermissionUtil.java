package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.world.entity.Entity;

public class PermissionUtil {
	public static boolean check(Entity entity, PermissionWrapper permission) {
		return Permissions.check(entity, permission.getNode(), permission.getPermissionLevel());
	}
}
