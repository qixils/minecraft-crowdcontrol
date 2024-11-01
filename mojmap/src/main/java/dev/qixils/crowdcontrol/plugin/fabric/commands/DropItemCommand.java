package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

@Getter
public class DropItemCommand extends ModdedCommand {
	private final String effectName = "drop_item";

	public DropItemCommand(@NotNull ModdedCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			boolean success = false;
			for (ServerPlayer player : playerSupplier.get()) {
				if (player.getInventory().getSelected().isEmpty()) continue;

				sync(() -> {
					player.drop(true);
					// for some reason the player's inventory is not getting updated
					// my code seems identical to the paper implementation, but maybe they have some
					//  weird listener that updates the inventory?
					// either way, this workaround is fine
					player.containerMenu.sendAllDataToRemote();
				});
				success = true;
			}
			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "No players were holding items");
		}));
	}
}
