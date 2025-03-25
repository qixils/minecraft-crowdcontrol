package dev.qixils.crowdcontrol.plugin.neoforge.util;

import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.util.Tristate;
import net.minecraft.world.entity.Entity;

public class LuckPermsPermissionUtil extends NeoForgePermissionUtil {
	private LuckPerms api;

	private void initializeApi() {
		try {
			api = LuckPermsProvider.get();
		} catch (Exception ignored) {}
	}

	@Override
	public boolean check(Entity entity, PermissionWrapper permission) {
		boolean fallback = super.check(entity, permission);

		initializeApi();
		if (api == null) return fallback;

		User user = api.getUserManager().getUser(entity.getUUID());
		if (user == null) return fallback;

		Tristate state = user.getCachedData().getPermissionData().checkPermission(permission.getNode());
		if (state == Tristate.UNDEFINED) return fallback;

		return state.asBoolean();
	}
}
