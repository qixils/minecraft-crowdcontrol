package dev.qixils.crowdcontrol.plugin.neoforge.util;

import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import dev.qixils.crowdcontrol.plugin.fabric.utils.PermissionUtil;
import net.minecraft.commands.PermissionSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class NeoForgePermissionUtil extends PermissionUtil {
	@Override
	public boolean check(Entity entity, PermissionWrapper permission) {
		if (entity instanceof Player player)
			return player.hasPermissions(permission.getPermissionLevel());
		return permission.getPermissionLevel() == 0;
	}

	@Override
	public boolean check(PermissionSource entity, PermissionWrapper permission) {
		return entity.hasPermission(permission.getPermissionLevel());
	}
}
