package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
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
	private final @NotNull ItemType item;
	private final @Nullable GoldenApple goldenAppleType;
	private final @NotNull String effectName;
	private final @NotNull String displayName;

	public TakeItemCommand(@NotNull SpongeCrowdControlPlugin plugin, @NotNull ItemType item, @Nullable GoldenApple goldenAppleType) {
		super(plugin);
		this.goldenAppleType = goldenAppleType;
		this.item = item;

		if (goldenAppleType == null || goldenAppleType.equals(GoldenApples.GOLDEN_APPLE)) {
			this.effectName = "take_" + SpongeTextUtil.valueOf(item);
			this.displayName = "Take " + item.getTranslation().get();
		} else {
			this.effectName = "take_enchanted_golden_apple";
			this.displayName = "Take Enchanted Golden Apple";
		}
	}

	public TakeItemCommand(@NotNull SpongeCrowdControlPlugin plugin, @NotNull ItemType item) {
		this(plugin, item, item.equals(ItemTypes.GOLDEN_APPLE) ? GoldenApples.GOLDEN_APPLE : null);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("Item could not be found in target inventories");
		for (Player player : players) {
			CarriedInventory<? extends Carrier> inventory = player.getInventory();
			for (Inventory invSlot : inventory.slots()) {
				Optional<ItemStack> optionalStack = invSlot.peek();
				if (!optionalStack.isPresent())
					continue;

				ItemStack stack = optionalStack.get();
				if (!stack.getType().equals(item))
					continue;
				if (goldenAppleType != null && !stack.get(Keys.GOLDEN_APPLE_TYPE).map(goldenAppleType::equals).orElse(false))
					continue;

				response.type(ResultType.SUCCESS).message("SUCCESS");
				sync(() -> {
					stack.setQuantity(stack.getQuantity() - 1);
					invSlot.set(stack);
				});
				break;
			}

			if (ResultType.SUCCESS == response.type() && item.equals(ItemTypes.END_PORTAL_FRAME))
				break;
		}
		return response;
	}
}
