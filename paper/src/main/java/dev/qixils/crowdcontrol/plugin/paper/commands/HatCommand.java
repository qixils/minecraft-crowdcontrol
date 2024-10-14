package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import static dev.qixils.crowdcontrol.plugin.paper.utils.ItemUtil.isSimilar;

@Getter
public class HatCommand extends RegionalCommandSync {
	private final String effectName = "hat";

	public HatCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(ResultType.RETRY)
			.message("Held item(s) and hat are the same");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
		PlayerInventory inv = player.getInventory();
		ItemStack head = inv.getItem(EquipmentSlot.HEAD);
		EquipmentSlot handSlot = EquipmentSlot.HAND;
		ItemStack hand = inv.getItem(handSlot);
		if (isSimilar(hand, head)) {
			handSlot = EquipmentSlot.OFF_HAND;
			hand = inv.getItem(handSlot);
			if (isSimilar(hand, head))
				return false;
		}
		inv.setItem(handSlot, head);
		inv.setItem(EquipmentSlot.HEAD, hand);
		return true;
	}
}
