package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.common.util.PermissionWrapper;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.entity.Entity;

public abstract class PermissionUtil {
	public abstract boolean check(Entity entity, PermissionWrapper permission);
	public abstract boolean check(SharedSuggestionProvider entity, PermissionWrapper permission);
}
