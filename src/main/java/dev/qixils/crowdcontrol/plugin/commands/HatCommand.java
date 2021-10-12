package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

@Getter
public class HatCommand extends ImmediateCommand {
	public HatCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	private final String effectName = "hat";
	private final String displayName = "Put Item on Head";

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull Request request) {
		CrowdControlPlugin.getPlayers().forEach(player -> {
			PlayerInventory inv = player.getInventory();
			ItemStack hand = inv.getItemInMainHand();
			ItemStack head = inv.getItem(EquipmentSlot.HEAD);
			inv.setItemInMainHand(head);
			inv.setItem(EquipmentSlot.HEAD, hand);
		});
		return Response.builder().type(Response.ResultType.SUCCESS);
	}
}
