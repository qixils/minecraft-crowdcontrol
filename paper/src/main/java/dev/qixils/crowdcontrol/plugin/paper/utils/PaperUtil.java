package dev.qixils.crowdcontrol.plugin.paper.utils;

import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PaperUtil {
	private PaperUtil() {}

	public static PermissionDefault toPaper(PermissionWrapper.DefaultPermission defaultPermission) {
		return switch (defaultPermission) {
			case ALL -> PermissionDefault.TRUE;
			case OP -> PermissionDefault.OP;
			case NONE -> PermissionDefault.FALSE;
        };
	}

	public static Permission toPaper(PermissionWrapper permission) {
		return new Permission(permission.getNode(), permission.getDescription(), toPaper(permission.getDefaultPermission()));
	}

	@NotNull
	public static NamespacedKey toPaper(@NotNull Key key) {
		if (key instanceof NamespacedKey nkKey)
			return nkKey;
		return new NamespacedKey(key.namespace(), key.value());
	}

	public static boolean hasPermission(Permissible permissible, Permission permission) {
		return switch (permissible.permissionValue(permission)) {
			case TRUE -> true;
			case FALSE -> false;
			default -> permission.getDefault().getValue(permissible.isOp());
		};
	}

	public static List<Player> toPlayers(@Nullable List<UUID> uuids) {
		if (uuids == null) return Collections.emptyList();
		return uuids.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
	}

	public static final Permission ADMIN_PERMISSION = toPaper(Plugin.ADMIN_PERMISSION);
}
