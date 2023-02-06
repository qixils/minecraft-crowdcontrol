package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.common.util.sound.Sounds;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.event.Listener;
import dev.qixils.crowdcontrol.plugin.fabric.event.Tick;
import dev.qixils.crowdcontrol.plugin.fabric.mixin.RecipeBookAccessor;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.POPUP_TITLE;

@Getter
@EventListener
public final class ToastCommand extends ImmediateCommand {
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
	private final String effectName = "toast";

	public ToastCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	public boolean isEventListener() {
		return true;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		sync(() -> players.forEach(player -> {
			// annoying sound
			player.playSound(Sounds.ANNOYING.get(), Sound.Emitter.self());

			// spam recipe toasts
			ServerRecipeBook book = player.getRecipeBook();
			RecipeManager recipeManager = player.getLevel().getRecipeManager();
			@SuppressWarnings("unchecked") // casting ? extends XYZ to XYZ is safe >_>
			Collection<Recipe<?>> recipes = ((RecipeBookAccessor) book).getKnown()
					.stream()
					.flatMap(location -> (Stream<Recipe<?>>) recipeManager.byKey(location).stream())
					.toList();
			book.removeRecipes(recipes, player);
			book.addRecipes(recipes, player);

			// pop-up inventory
			Container container = new SimpleContainer(INVENTORY_SIZE);
			ToastInventory toastInv = new ToastInventory(container);
			toastInv.tick();
			player.openMenu(new ToastMenuProvider(container, plugin.adventure().toNative(plugin.renderForPlayer(POPUP_TITLE, player))));
			OPEN_INVENTORIES.put(player.getUUID(), toastInv);
		}));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
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

	private record ToastMenuProvider(@NotNull Container container, @NotNull Component displayName) implements MenuProvider {
		@Override
		@NotNull
		public Component getDisplayName() {
			return displayName;
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
