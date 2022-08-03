package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
public final class RemoveEnchantsCommand extends ImmediateCommand {
	private final String effectName = "remove_enchants";
	private final String displayName = "Remove Enchants";

	public RemoveEnchantsCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder result = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Target was not holding an enchanted item");

		for (ServerPlayer player : players) {
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				if (tryRemoveEnchants(player.getItemBySlot(slot))) {
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
		ListTag enchantments = item.getEnchantmentTags();
		if (enchantments.isEmpty())
			return false;
		enchantments.clear(); // TODO: test that this works; might need to item.getTag().put(...)?
		return true;
	}
}
