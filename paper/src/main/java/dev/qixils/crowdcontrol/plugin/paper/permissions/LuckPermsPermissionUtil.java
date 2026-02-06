package dev.qixils.crowdcontrol.plugin.paper.permissions;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.util.Tristate;
import org.bukkit.entity.Entity;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;

import java.util.Optional;
import java.util.UUID;

public class LuckPermsPermissionUtil extends PaperPermissionUtil implements PermissionUtil {
	private final LuckPerms api;

	public LuckPermsPermissionUtil(LuckPerms luckPerms) {
		this.api = luckPerms;
		if (luckPerms == null) throw new IllegalArgumentException("luckPerms undefined");
	}

	@SuppressWarnings("UnstableApiUsage")
	private UUID getUUID(Permissible permissible) {
		if (permissible instanceof Entity entity) return entity.getUniqueId();
		if (permissible instanceof Identified identified) return identified.identity().uuid();
		if (permissible instanceof Audience audience) {
			Optional<UUID> uuidOpt = audience.pointers().get(Identity.UUID);
			if (uuidOpt.isPresent()) return uuidOpt.get();
		}
		if (permissible instanceof CommandSourceStack stack) return getUUID(stack.getSender());
		return null;
	}

	@Override
	public boolean hasPermission(Permissible permissible, Permission permission) {
		try {
			UUID uuid = getUUID(permissible);
			if (uuid == null) return super.hasPermission(permissible, permission);

			User user = api.getUserManager().getUser(uuid);
			if (user == null) return super.hasPermission(permissible, permission);

			Tristate state = user.getCachedData().getPermissionData().checkPermission(permission.getName());
			if (state == Tristate.UNDEFINED) return super.hasPermission(permissible, permission);

			return state.asBoolean();
		} catch (Exception ignored) {
			return super.hasPermission(permissible, permission);
		}
	}
}
