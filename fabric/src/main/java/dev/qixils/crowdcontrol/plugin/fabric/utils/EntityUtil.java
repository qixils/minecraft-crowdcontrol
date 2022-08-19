package dev.qixils.crowdcontrol.plugin.fabric.utils;

import dev.qixils.crowdcontrol.plugin.fabric.commands.KeepInventoryCommand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;

public class EntityUtil {
	private EntityUtil() {
	}

	public static boolean keepInventoryRedirect(Entity entity, GameRules gameRules, GameRules.Key<BooleanValue> key) {
		// get server's default value
		boolean keepInventory = gameRules.getBoolean(key);

		// double check that this is the keep inventory game rule
		if (key != GameRules.RULE_KEEPINVENTORY)
			return keepInventory;

		// don't redirect if the game rule is enabled
		//   TODO: maybe hide the disable/enable keep inventory command
		//         when the game rule is enabled?
		if (keepInventory)
			return true;

		// return whether the keep inventory command is active for this user
		return KeepInventoryCommand.isKeepingInventory(entity);
	}
}
