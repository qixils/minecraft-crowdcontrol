package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.common.command.QuantityStyle;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.GoldenApple;
import org.spongepowered.api.data.type.GoldenApples;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.type.CarriedInventory;

import java.util.List;
import java.util.Optional;

// the handling of enchanted golden apples is unfortunately hacky and hardcoded because I was having
// issues trying to get Key<?> generics to work

@Getter
public class TakeItemCommand extends ImmediateCommand {
	private final @NotNull QuantityStyle quantityStyle = QuantityStyle.APPEND_X;
	private final @NotNull ItemType item;
	private final @Nullable GoldenApple goldenAppleType;
	private final @NotNull String effectName;
	private final @NotNull TranslatableComponent defaultDisplayName;

	public TakeItemCommand(@NotNull SpongeCrowdControlPlugin plugin, @NotNull ItemType item, @Nullable GoldenApple goldenAppleType) {
		super(plugin);
		this.goldenAppleType = goldenAppleType;
		this.item = item;

		Component arg;
		if (goldenAppleType == null || goldenAppleType.equals(GoldenApples.GOLDEN_APPLE)) {
			this.effectName = "take_" + SpongeTextUtil.valueOf(item);
			arg = Component.translatable(item.getTranslation().getId());
		} else {
			this.effectName = "take_enchanted_golden_apple";
			arg = Component.translatable("cc.item.enchanted_golden_apple.name");
		}

		this.defaultDisplayName = Component.translatable("cc.effect.take_item.name", arg);
	}

	public TakeItemCommand(@NotNull SpongeCrowdControlPlugin plugin, @NotNull ItemType item) {
		this(plugin, item, item.equals(ItemTypes.GOLDEN_APPLE) ? GoldenApples.GOLDEN_APPLE : null);
	}

	private boolean takeItemFrom(Player player, int amount) {
		CarriedInventory<? extends Carrier> inventory = player.getInventory();
		// simulate
		int toTake = 0;
		for (Inventory invSlot : inventory.slots()) {
			Optional<ItemStack> optionalStack = invSlot.peek();
			if (!optionalStack.isPresent()) continue;
			ItemStack stack = optionalStack.get();
			if (!stack.getType().equals(item)) continue;
			if (goldenAppleType != null && !stack.get(Keys.GOLDEN_APPLE_TYPE).map(goldenAppleType::equals).orElse(false)) continue;
			toTake += stack.getQuantity();
		}
		// do
		if (toTake < amount) return false;
		toTake = amount;
		for (Inventory invSlot : inventory.slots()) {
			Optional<ItemStack> optionalStack = invSlot.peek();
			if (!optionalStack.isPresent()) continue;
			ItemStack stack = optionalStack.get();
			if (!stack.getType().equals(item)) continue;
			if (goldenAppleType != null && !stack.get(Keys.GOLDEN_APPLE_TYPE).map(goldenAppleType::equals).orElse(false)) continue;
			int take = Math.min(toTake, stack.getQuantity());
			toTake -= take;
			sync(() -> {
				stack.setQuantity(stack.getQuantity() - take);
				invSlot.set(stack);
			});
			if (toTake == 0) break;
		}
		return true;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		int amount = request.getQuantityOrDefault();

		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Item could not be found in target inventories");

		LimitConfig config = getPlugin().getLimitConfig();
		int victims = 0;
		int maxVictims = config.getItemLimit(SpongeTextUtil.valueOf(item.getType()));

		// first pass (hosts)
		for (Player player : players) {
			if (!config.hostsBypass() && maxVictims > 0 && victims >= maxVictims)
				break;
			if (!isHost(player))
				continue;
			if (takeItemFrom(player, amount))
				victims++;
		}

		// second pass (guests)
		for (Player player : players) {
			if (maxVictims > 0 && victims >= maxVictims)
				break;
			if (isHost(player))
				continue;
			if (takeItemFrom(player, amount))
				victims++;
		}

		if (victims > 0)
			response.type(ResultType.SUCCESS).message("SUCCESS");

		return response;
	}
}
