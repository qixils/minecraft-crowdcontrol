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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerRecipeBook;
import net.minecraft.text.Text;
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
	private static Text TITLE;
	private final String effectName = "toast";

	public ToastCommand(FabricCrowdControlPlugin plugin) {
		super(plugin);
		TITLE = plugin.adventure().toNative(POPUP_TITLE);
	}

	@Override
	public boolean isEventListener() {
		return true;
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		sync(() -> players.forEach(player -> {
			// annoying sound
			player.playSound(Sounds.ANNOYING.get(), Sound.Emitter.self());

			// spam recipe toasts
			ServerRecipeBook book = player.getRecipeBook();
			RecipeManager recipeManager = player.getWorld().getRecipeManager();
			@SuppressWarnings("unchecked") // casting ? extends XYZ to XYZ is safe >_>
			Collection<Recipe<?>> recipes = ((RecipeBookAccessor) book).getKnown()
					.stream()
					.flatMap(location -> (Stream<Recipe<?>>) recipeManager.get(location).stream())
					.toList();
			book.lockRecipes(recipes, player);
			book.unlockRecipes(recipes, player);

			// pop-up inventory
			Inventory container = new SimpleInventory(INVENTORY_SIZE);
			ToastInventory toastInv = new ToastInventory(container);
			toastInv.tick();
			player.openHandledScreen(new ToastMenuProvider(container));
			OPEN_INVENTORIES.put(player.getUuid(), toastInv);
		}));
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	@Listener
	public void onTick(@NotNull Tick tick) {
		OPEN_INVENTORIES.values().forEach(ToastInventory::tick);
	}

	private static final class ToastInventory {
		private final @NotNull Inventory inventory;
		private int index = 0;

		private ToastInventory(@NotNull Inventory inventory) {
			this.inventory = inventory;
		}

		public void tick() {
			Item item = MATERIALS[index++ % MATERIALS.length];
			for (int i = 0; i < INVENTORY_SIZE; i++) {
				inventory.setStack(i, new ItemStack(item));
			}
		}
	}

	private record ToastMenuProvider(@NotNull Inventory container) implements NamedScreenHandlerFactory {
		@Override
		@NotNull
		public Text getDisplayName() {
			return TITLE;
		}

		@Override
		public ScreenHandler createMenu(int i, @NotNull PlayerInventory inventory, @NotNull PlayerEntity player) {
			return new ToastMenu(i, inventory, container);
		}
	}

	private static final class ToastMenu extends GenericContainerScreenHandler {
		public ToastMenu(int i, @NotNull PlayerInventory inventory, @NotNull Inventory container) {
			super(ScreenHandlerType.GENERIC_9X3, i, inventory, container, 3);
		}

		@Override
		public void onClosed(@NotNull PlayerEntity player) {
			super.onClosed(player);
			OPEN_INVENTORIES.remove(player.getUuid());
		}

		@Override
		public void onSlotClick(int slotIndex, int buttonIndex, @NotNull SlotActionType clickType, @NotNull PlayerEntity player) {
			if (!(player instanceof ServerPlayerEntity sPlayer))
				return;
			sPlayer.currentScreenHandler.syncState();
			sPlayer.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, -1, sPlayer.playerScreenHandler.nextRevision(), sPlayer.currentScreenHandler.getCursorStack()));
			if (slotIndex >= 0 && slotIndex < sPlayer.currentScreenHandler.slots.size())
				sPlayer.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(sPlayer.currentScreenHandler.syncId, sPlayer.playerScreenHandler.nextRevision(), slotIndex, sPlayer.currentScreenHandler.getSlot(slotIndex).getStack()));
		}
	}
}
