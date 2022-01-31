package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import com.flowpowered.math.vector.Vector3d;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class ToastCommand extends ImmediateCommand {
	private static final Text TITLE = Text.of("Pop-Up");
	private static final ItemType ITEM = ItemTypes.STAINED_GLASS_PANE;
	private static final DyeColor[] COLORS = new DyeColor[]{
			DyeColors.BROWN,
			DyeColors.RED,
			DyeColors.ORANGE,
			DyeColors.YELLOW,
			DyeColors.LIME,
			DyeColors.GREEN,
			DyeColors.CYAN,
			DyeColors.LIGHT_BLUE,
			DyeColors.BLUE,
			DyeColors.PURPLE,
			DyeColors.MAGENTA,
			DyeColors.PINK,
			DyeColors.WHITE,
			DyeColors.SILVER,
			DyeColors.GRAY,
			DyeColors.BLACK
	};
	private static final int INVENTORY_SIZE = 9 * 3;
	private final String effectName = "toast";
	private final String displayName = "Annoying Pop-Ups";

	public ToastCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		sync(() -> {
			for (Player player : players) {
				Vector3d pos = player.getPosition();
				plugin.asAudience(player).playSound(Sounds.ANNOYING.get(), pos.getX(), pos.getY(), pos.getZ());
				// API8: recipes

				// actual pop-up
				Inventory inv = Inventory.builder()
						.of(InventoryArchetypes.CHEST)
						.listener(ClickInventoryEvent.class, event -> event.setCancelled(true))
						.property(new InventoryTitle(TITLE))
						.build(plugin);
				sync(() -> player.openInventory(inv));

				AtomicInteger atomicIndex = new AtomicInteger(random.nextInt(COLORS.length));
				// future ensures the task doesn't get cancelled before the inventory is opened
				CompletableFuture<Void> hasStarted = new CompletableFuture<>();

				Task.builder()
						.intervalTicks(2)
						.execute(task -> {
							// this check isn't perfect but it is good enough
							if (!hasStarted.complete(null) && !player.isViewingInventory()) {
								task.cancel();
								return;
							}
							int index = atomicIndex.getAndIncrement();
							DyeColor color = COLORS[index % COLORS.length];
							ItemStack.Builder builder = ItemStack.builder()
									.itemType(ITEM)
									.add(Keys.DYE_COLOR, color);

							inv.clear();
							for (Inventory slot : inv.slots()) {
								slot.offer(builder.build());
							}
						})
						.submit(plugin);
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	@Override
	public boolean isEventListener() {
		return true;
	}
}
