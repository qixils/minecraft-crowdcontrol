package dev.qixils.crowdcontrol.plugin.neoforge.util;

import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.util.Tristate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
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
		try {
			initializeApi();
			if (api == null) return super.check(entity, permission);

			User user = api.getUserManager().getUser(entity.getUUID());
			if (user == null) return super.check(entity, permission);

			Tristate state = user.getCachedData().getPermissionData().checkPermission(permission.getNode());
			if (state == Tristate.UNDEFINED) return super.check(entity, permission);

			return state.asBoolean();
		} catch (Exception ignored) {
			return super.check(entity, permission);
		}
	}

	@Override
	public boolean check(SharedSuggestionProvider source, PermissionWrapper permission) {
		try {
			initializeApi();
			if (api == null) return super.check(source, permission);

			if (!(source instanceof CommandSourceStack stack)) return super.check(source, permission);

			Entity entity = stack.getEntity();
			if (entity == null) return super.check(source, permission);

			User user = api.getUserManager().getUser(entity.getUUID());
			if (user == null) return super.check(source, permission);

			Tristate state = user.getCachedData().getPermissionData().checkPermission(permission.getNode());
			if (state == Tristate.UNDEFINED) return super.check(source, permission);

			return state.asBoolean();
		} catch (Exception ignored) {
			return super.check(source, permission);
		}
	}
}
