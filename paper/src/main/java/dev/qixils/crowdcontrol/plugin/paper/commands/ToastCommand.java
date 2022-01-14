package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class ToastCommand extends ImmediateCommand implements Listener {
	private static final Component TITLE = Component.text("Pop-Up");
	private static final Material[] MATERIALS = new Material[]{
			Material.BROWN_STAINED_GLASS_PANE,
			Material.RED_STAINED_GLASS_PANE,
			Material.ORANGE_STAINED_GLASS_PANE,
			Material.YELLOW_STAINED_GLASS_PANE,
			Material.LIME_STAINED_GLASS_PANE,
			Material.GREEN_STAINED_GLASS_PANE,
			Material.CYAN_STAINED_GLASS_PANE,
			Material.LIGHT_BLUE_STAINED_GLASS_PANE,
			Material.BLUE_STAINED_GLASS_PANE,
			Material.PURPLE_STAINED_GLASS_PANE,
			Material.MAGENTA_STAINED_GLASS_PANE,
			Material.PINK_STAINED_GLASS_PANE,
			Material.WHITE_STAINED_GLASS_PANE,
			Material.LIGHT_GRAY_STAINED_GLASS_PANE,
			Material.GRAY_STAINED_GLASS_PANE,
			Material.BLACK_STAINED_GLASS_PANE
	};
	private static final int INVENTORY_SIZE = 9 * 3;
	private final Map<UUID, Inventory> openInventories = new HashMap<>();
	private final String effectName = "toast";
	private final String displayName = "Annoying Pop-Ups";

	public ToastCommand(PaperCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		sync(() -> {
			for (Player player : players) {
				player.playSound(Sounds.ANNOYING, player);
				Collection<NamespacedKey> recipes = player.getDiscoveredRecipes();
				player.undiscoverRecipes(recipes);
				player.discoverRecipes(recipes);

				// actual pop-up
				Inventory inv = Bukkit.getServer().createInventory(player, INVENTORY_SIZE, TITLE);
				openInventories.put(player.getUniqueId(), inv);
				sync(() -> player.openInventory(inv));

				AtomicInteger atomicIndex = new AtomicInteger(random.nextInt(MATERIALS.length));
				// future ensures the task doesn't get cancelled before the inventory is opened
				CompletableFuture<Void> hasStarted = new CompletableFuture<>();

				Bukkit.getScheduler().runTaskTimer(plugin, task -> {
					if (!hasStarted.complete(null) && inv.getViewers().isEmpty()) {
						task.cancel();
						openInventories.remove(player.getUniqueId(), inv);
						return;
					}
					int index = atomicIndex.getAndIncrement();
					Material material = MATERIALS[index % MATERIALS.length];
					ItemStack[] contents = new ItemStack[INVENTORY_SIZE];
					for (int i = 0; i < INVENTORY_SIZE; i++) {
						contents[i] = new ItemStack(material);
					}
					inv.setStorageContents(contents);
				}, 0, 2);
			}
		});
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	// prevent users from taking items from the pop-up inventory
	// note: bukkit is ridiculously stupid and does not let you directly listen to this event, nor
	// the entire InventoryEvent (despite that one not even being abstract???) so you have to
	// manually listen to each inventory event
	public void onInventoryEvent(InventoryInteractEvent event) {
		UUID uuid = event.getWhoClicked().getUniqueId();
		Inventory inv = openInventories.get(uuid);
		if (inv == null)
			return;

		if (inv.getViewers().isEmpty()) {
			openInventories.remove(uuid);
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClickEvent(InventoryClickEvent event) {
		onInventoryEvent(event);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryCreativeEvent(InventoryCreativeEvent event) {
		onInventoryEvent(event);
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryDragEvent(InventoryDragEvent event) {
		onInventoryEvent(event);
	}
}
