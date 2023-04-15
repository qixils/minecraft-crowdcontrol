package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.ITEM_DAMAGE_PERCENTAGE;

@Getter
public abstract class ItemDurabilityCommand extends ImmediateCommand {
	private final String effectName;

	protected ItemDurabilityCommand(FabricCrowdControlPlugin plugin, String effectName) {
		super(plugin);
		this.effectName = effectName;
	}

	protected abstract int modifyDurability(int curDamage, int maxDamage);

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Targets not holding a durable item");

		// create list of random equipment slots
		List<EquipmentSlot> slots = Arrays.asList(EquipmentSlot.values());
		Collections.shuffle(slots);

		// loop through all players and all slots, and apply the durability change
		for (ServerPlayer player : players) {
			for (EquipmentSlot slot : slots) {
				ItemStack item = player.getItemBySlot(slot);
				if (item.isEmpty())
					continue;
				if (!item.isDamageableItem())
					continue;
				int curDamage = item.getDamageValue();
				int maxDamage = item.getMaxDamage();
				int newDamage = Math.min(maxDamage, Math.max(0, modifyDurability(curDamage, maxDamage)));

				if (!CommandConstants.canApplyDamage(curDamage, newDamage, maxDamage))
					continue;

				result.type(ResultType.SUCCESS).message("SUCCESS");
				item.setDamageValue(newDamage);
				break;
			}
		}

		return result;
	}

	// repair command
	@NotNull
	public static final class Repair extends ItemDurabilityCommand {
		public Repair(FabricCrowdControlPlugin plugin) {
			super(plugin, "repair_item");
		}

		@Override
		protected int modifyDurability(int curDamage, int maxDamage) {
			return 0;
		}
	}

	// damage command (cuts the usable durability in half)
	@NotNull
	public static final class Damage extends ItemDurabilityCommand {
		public Damage(FabricCrowdControlPlugin plugin) {
			super(plugin, "damage_item");
		}

		@Override
		protected int modifyDurability(int curDamage, int maxDamage) {
			return curDamage + (maxDamage / ITEM_DAMAGE_PERCENTAGE);
		}
	}
}
