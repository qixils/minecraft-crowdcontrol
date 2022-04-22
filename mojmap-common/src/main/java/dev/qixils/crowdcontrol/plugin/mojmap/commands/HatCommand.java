package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static dev.qixils.crowdcontrol.plugin.mojmap.utils.ItemUtil.isSimilar;

@Getter
public class HatCommand extends ImmediateCommand {
	private final String effectName = "hat";
	private final String displayName = "Put Item on Head";

	public HatCommand(MojmapPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Held item(s) and hat are the same");

		for (ServerPlayer player : players) {
			ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
			for (InteractionHand handType : InteractionHand.values()) {
				ItemStack hand = player.getItemInHand(handType);
				if (isSimilar(hand, head))
					continue;
				response.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> {
					player.setItemSlot(EquipmentSlot.HEAD, hand);
					player.setItemInHand(handType, head);
				});
				break;
			}
		}

		return response;
	}
}
