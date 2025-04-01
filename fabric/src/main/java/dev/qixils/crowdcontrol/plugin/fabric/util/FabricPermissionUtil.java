package dev.qixils.crowdcontrol.plugin.fabric.util;

import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import dev.qixils.crowdcontrol.plugin.fabric.utils.PermissionUtil;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.Entity;

public class FabricPermissionUtil extends PermissionUtil {
	@Override
	public boolean check(Entity entity, PermissionWrapper permission) {
		return Permissions.check(entity, permission.getNode(), permission.getPermissionLevel());
	}

	@Override
	public boolean check(SharedSuggestionProvider entity, PermissionWrapper permission) {
		return Permissions.check(entity, permission.getNode(), permission.getPermissionLevel());
	}
}
