package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
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
	protected @NotNull CCEffectResponse buildFailure(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Held item(s) and hat are the same");
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
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
