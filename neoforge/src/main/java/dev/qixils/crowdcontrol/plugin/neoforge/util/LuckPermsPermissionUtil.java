package dev.qixils.crowdcontrol.plugin.neoforge.util;

import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.util.Tristate;
import net.minecraft.world.entity.Entity;

public class LuckPermsPermissionUtil extends NeoForgePermissionUtil {
	private final LuckPerms api = LuckPermsProvider.get();

	@Override
	public boolean check(Entity entity, PermissionWrapper permission) {
		User user = api.getUserManager().getUser(entity.getUUID());
		if (user == null) return super.check(entity, permission);

		Tristate state = user.getCachedData().getPermissionData().checkPermission(permission.getNode());
		if (state == Tristate.UNDEFINED) return super.check(entity, permission);

		return state.asBoolean();
	}
}
