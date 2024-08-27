package dev.qixils.crowdcontrol.plugin.neoforge.util;

import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import dev.qixils.crowdcontrol.plugin.fabric.utils.PermissionUtil;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

import java.util.HashMap;
import java.util.Map;

public class NeoForgePermissionUtil extends PermissionUtil {
	private final Map<String, PermissionNode<Boolean>> nodes = new HashMap<>();

	@Override
	public boolean check(Entity entity, PermissionWrapper permission) {
		return false; // TODO: maybe only support LP, cus this perms API kinda stinky, lol
	}
}
