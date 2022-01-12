package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class HatCommand extends ImmediateCommand {
	private final String effectName = "hat";
	private final String displayName = "Put Item on Head";

	public HatCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Held item(s) and hat are the same");
		for (Player player : players) {
			PlayerInventory inv = player.getInventory();
			ItemStack head = inv.getItem(EquipmentSlot.HEAD);
			EquipmentSlot handSlot = EquipmentSlot.HAND;
			ItemStack hand = inv.getItem(handSlot);
			if (isSimilar(hand, head)) {
				handSlot = EquipmentSlot.OFF_HAND;
				hand = inv.getItem(handSlot);
				if (isSimilar(hand, head))
					continue;
			}
			response.type(ResultType.SUCCESS).message("SUCCESS");
			inv.setItem(handSlot, head);
			inv.setItem(EquipmentSlot.HEAD, hand);
		}
		return response;
	}

	private boolean isSimilar(@Nullable ItemStack item1, @Nullable ItemStack item2) {
		if (item1 == null && item2 == null)
			return true;
		if (item1 == null)
			return false;
		if (item2 == null)
			return false;
		return item1.isSimilar(item2);
	}
}
