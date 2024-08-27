package dev.qixils.crowdcontrol.plugin.neoforge.util;

import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import dev.qixils.crowdcontrol.plugin.fabric.utils.PermissionUtil;
import net.minecraft.world.entity.Entity;

public class NeoForgePermissionUtil extends PermissionUtil {
	@Override
	public boolean check(Entity entity, PermissionWrapper permission) {
		// TODO: node API support
		return entity.hasPermissions(permission.getPermissionLevel());
	}
}
