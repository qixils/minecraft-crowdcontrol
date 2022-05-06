package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.common.CommandConstants;
import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public abstract class ItemDurabilityCommand extends ImmediateCommand {
	private final String effectName;
	private final String displayName;

	protected ItemDurabilityCommand(MojmapPlugin<?> plugin, String displayName) {
		super(plugin);
		this.displayName = displayName;
		this.effectName = displayName.replace(' ', '_');
	}

	protected abstract int modifyDurability(int curDamage, int maxDamage);

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Targets not holding a durable item");

		for (ServerPlayer player : players) {
			for (InteractionHand hand : InteractionHand.values()) {
				ItemStack item = player.getItemInHand(hand);
				if (item.isEmpty())
					continue;
				int curDamage = item.getDamageValue();
				int maxDamage = item.getMaxDamage();
				int newDamage = modifyDurability(curDamage, maxDamage);

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
	public static ItemDurabilityCommand repair(MojmapPlugin<?> plugin) {
		return new ItemDurabilityCommand(plugin, "Repair Item") {
			@Override
			protected int modifyDurability(int curDamage, int maxDamage) {
				return 0;
			}
		};
	}

	// damage command (cuts the usable durability in half)
	@NotNull
	public static ItemDurabilityCommand damage(MojmapPlugin<?> plugin) {
		return new ItemDurabilityCommand(plugin, "Damage Item") {
			@Override
			protected int modifyDurability(int curDamage, int maxDamage) {
				return (maxDamage + curDamage) / 2;
			}
		};
	}
}
