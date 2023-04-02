package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public final class RemoveEnchantsCommand extends ImmediateCommand {
	private final String effectName = "remove_enchants";

	public RemoveEnchantsCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Target was not holding an enchanted item");

		for (ServerPlayerEntity player : players) {
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				if (tryRemoveEnchants(player.getEquippedStack(slot))) {
					result.type(ResultType.SUCCESS).message("SUCCESS");
					break;
				}
			}
		}
		return result;
	}

	@Contract
	private boolean tryRemoveEnchants(ItemStack item) {
		if (item.isEmpty())
			return false;
		NbtList enchantments = item.getEnchantments();
		if (enchantments.isEmpty())
			return false;
		enchantments.clear();
		return true;
	}
}
