package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ContainerTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POPUP_TITLE;

@Getter
@EventListener
public class ToastCommand extends ImmediateCommand {
	private static final ItemType[] MATERIALS = new ItemType[]{
			ItemTypes.BROWN_STAINED_GLASS_PANE.get(),
			ItemTypes.RED_STAINED_GLASS_PANE.get(),
			ItemTypes.ORANGE_STAINED_GLASS_PANE.get(),
			ItemTypes.YELLOW_STAINED_GLASS_PANE.get(),
			ItemTypes.LIME_STAINED_GLASS_PANE.get(),
			ItemTypes.GREEN_STAINED_GLASS_PANE.get(),
			ItemTypes.CYAN_STAINED_GLASS_PANE.get(),
			ItemTypes.LIGHT_BLUE_STAINED_GLASS_PANE.get(),
			ItemTypes.BLUE_STAINED_GLASS_PANE.get(),
			ItemTypes.PURPLE_STAINED_GLASS_PANE.get(),
			ItemTypes.MAGENTA_STAINED_GLASS_PANE.get(),
			ItemTypes.PINK_STAINED_GLASS_PANE.get(),
			ItemTypes.WHITE_STAINED_GLASS_PANE.get(),
			ItemTypes.LIGHT_GRAY_STAINED_GLASS_PANE.get(),
			ItemTypes.GRAY_STAINED_GLASS_PANE.get(),
			ItemTypes.BLACK_STAINED_GLASS_PANE.get()
	};
	private static final int INVENTORY_SIZE = 9 * 3;
	private final String effectName = "toast";

	public ToastCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		sync(() -> {
			for (ServerPlayer player : players) {
				player.playSound(Sounds.ANNOYING.get(), Sound.Emitter.self());
				// API8: recipes | TODO: still no API for it; may need to open an issue

				// actual pop-up
				ViewableInventory inv = ViewableInventory.builder()
						.type(ContainerTypes.GENERIC_9X3)
						.fillDummy()
						.completeStructure()
						.plugin(plugin.getPluginContainer())
						.build()
						.asViewable()
						.orElseThrow(() -> new IllegalStateException("Could not create custom inventory"));
				InventoryMenu menu = inv.asMenu();
				menu.setTitle(plugin.renderForPlayer(POPUP_TITLE, player));
				menu.setReadOnly(true);
				sync(() -> menu.open(player));

				AtomicInteger atomicIndex = new AtomicInteger(random.nextInt(MATERIALS.length));
				// future ensures the task doesn't get cancelled before the inventory is opened
				CompletableFuture<Void> hasStarted = new CompletableFuture<>();

				plugin.getSyncScheduler().submit(Task.builder()
						.interval(Ticks.of(2))
						.execute(task -> {
							// this check isn't perfect but it is good enough
							if (!hasStarted.complete(null) && !player.isViewingInventory()) {
								task.cancel();
								return;
							}
							int index = atomicIndex.getAndIncrement();
							ItemType item = MATERIALS[index % MATERIALS.length];
							inv.clear();
							for (Slot slot : inv.slots()) {
								// slots seem to be duplicated in the ViewableInventory,
								// so skip an iteration if a non-empty slot is encountered
								if (!slot.peek().isEmpty())
									continue;
								// set item
								slot.offer(ItemStack.of(item, 1));
							}
						})
						.plugin(plugin.getPluginContainer())
						.build());
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}
}
