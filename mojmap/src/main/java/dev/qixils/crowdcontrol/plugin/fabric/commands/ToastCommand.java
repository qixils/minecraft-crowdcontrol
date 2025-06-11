package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.plugin.fabric.event.Tick;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.sound.Sound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POPUP_TITLE;

@Getter
@EventListener
public final class ToastCommand extends ModdedCommand {
	private static final Item[] MATERIALS = new Item[]{
			Items.BROWN_STAINED_GLASS_PANE,
			Items.RED_STAINED_GLASS_PANE,
			Items.ORANGE_STAINED_GLASS_PANE,
			Items.YELLOW_STAINED_GLASS_PANE,
			Items.LIME_STAINED_GLASS_PANE,
			Items.GREEN_STAINED_GLASS_PANE,
			Items.CYAN_STAINED_GLASS_PANE,
			Items.LIGHT_BLUE_STAINED_GLASS_PANE,
			Items.BLUE_STAINED_GLASS_PANE,
			Items.PURPLE_STAINED_GLASS_PANE,
			Items.MAGENTA_STAINED_GLASS_PANE,
			Items.PINK_STAINED_GLASS_PANE,
			Items.WHITE_STAINED_GLASS_PANE,
			Items.LIGHT_GRAY_STAINED_GLASS_PANE,
			Items.GRAY_STAINED_GLASS_PANE,
			Items.BLACK_STAINED_GLASS_PANE
	};
	private static final int INVENTORY_SIZE = 9 * 3;
	private static final Map<UUID, ToastInventory> OPEN_INVENTORIES = new HashMap<>();
	private static Component TITLE;
	private final String effectName = "toast";

	public ToastCommand(ModdedCrowdControlPlugin plugin) {
		super(plugin);
		TITLE = plugin.adventure().asNative(POPUP_TITLE);
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			for (ServerPlayer player : playerSupplier.get()) {
				// annoying sound
				player.playSound(Sounds.ANNOYING.get(), Sound.Emitter.self());

				// spam recipe toasts
				ServerRecipeBook book = player.getRecipeBook();
				RecipeManager recipeManager = plugin.server().getRecipeManager();
				Collection<RecipeHolder<?>> recipes = book.known
					.stream()
					.flatMap(location -> recipeManager.byKey(location).stream())
					.toList();
				book.removeRecipes(recipes, player);
				book.addRecipes(recipes, player);

				// pop-up inventory
				Container container = new SimpleContainer(INVENTORY_SIZE);
				ToastInventory toastInv = new ToastInventory(container);
				toastInv.tick();
				player.openMenu(new ToastMenuProvider(container));
				OPEN_INVENTORIES.put(player.getUUID(), toastInv);
			}
			return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS);
		}, plugin.getSyncExecutor()));
	}

	@Listener
	public void onTick(@NotNull Tick tick) {
		OPEN_INVENTORIES.values().forEach(ToastInventory::tick);
	}

	private static final class ToastInventory {
		private final @NotNull Container inventory;
		private int index = 0;

		private ToastInventory(@NotNull Container inventory) {
			this.inventory = inventory;
		}

		public void tick() {
			Item item = MATERIALS[index++ % MATERIALS.length];
			for (int i = 0; i < INVENTORY_SIZE; i++) {
				inventory.setItem(i, new ItemStack(item));
			}
		}
	}

	private record ToastMenuProvider(@NotNull Container container) implements MenuProvider {
		@Override
		@NotNull
		public Component getDisplayName() {
			return TITLE;
		}

		@Override
		public AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
			return new ToastMenu(i, inventory, container);
		}
	}

	private static final class ToastMenu extends ChestMenu {
		public ToastMenu(int i, @NotNull Inventory inventory, @NotNull Container container) {
			super(MenuType.GENERIC_9x3, i, inventory, container, 3);
		}

		@Override
		public void removed(@NotNull Player player) {
			super.removed(player);
			OPEN_INVENTORIES.remove(player.getUUID());
		}

		@Override
		public void clicked(int slotIndex, int buttonIndex, @NotNull ClickType clickType, @NotNull Player player) {
			if (!(player instanceof ServerPlayer sPlayer))
				return;
			sPlayer.containerMenu.sendAllDataToRemote();
			sPlayer.connection.send(new ClientboundContainerSetSlotPacket(-1, -1, sPlayer.inventoryMenu.incrementStateId(), sPlayer.containerMenu.getCarried()));
			if (slotIndex >= 0 && slotIndex < sPlayer.containerMenu.slots.size())
				sPlayer.connection.send(new ClientboundContainerSetSlotPacket(sPlayer.containerMenu.containerId, sPlayer.inventoryMenu.incrementStateId(), slotIndex, sPlayer.containerMenu.getSlot(slotIndex).getItem()));
		}
	}
}
