package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.common.command.QuantityStyle;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

@Getter
public class GiveItemCommand extends ModdedCommand implements ItemCommand {
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND_X;
	private final Item item;
	private final String effectName;
	private final TranslatableComponent defaultDisplayName;

	public GiveItemCommand(ModdedCrowdControlPlugin plugin, Item item) {
		super(plugin);
		this.item = item;
		this.effectName = "give_" + BuiltInRegistries.ITEM.getKey(item).getPath();
		this.defaultDisplayName = Component.translatable("cc.effect.give_item.name", plugin.toAdventure(item.getName(new ItemStack(item))));
	}

	@Blocking
	public static void giveItemTo(ServerPlayer player, ItemStack itemStack) {
		ItemEntity entity = player.spawnAtLocation(player.serverLevel(), itemStack);
		if (entity == null)
			throw new IllegalStateException("Could not spawn item entity");
		entity.setTarget(player.getUUID());
		entity.setThrower(player);
		entity.setPickUpDelay(0);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			List<ServerPlayer> players = playerSupplier.get();

			LimitConfig config = getPlugin().getLimitConfig();
			int playerLimit = config.getItemLimit(BuiltInRegistries.ITEM.getKey(item).getPath());

			int amount = request.getQuantity();
			ItemStack itemStack = new ItemStack(item, amount);

			return executeLimit(request, players, playerLimit, player -> {
				sync(() -> giveItemTo(player, itemStack));
				return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
			});
		}));
	}
}
