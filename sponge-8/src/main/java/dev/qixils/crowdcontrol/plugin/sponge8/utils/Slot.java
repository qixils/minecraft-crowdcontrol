package dev.qixils.crowdcontrol.plugin.sponge8.utils;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

public enum Slot {
	MAIN_HAND {
		@Override
		public @NotNull ItemStack getItem(Player player) {
			return player.itemInHand(HandTypes.MAIN_HAND);
		}
	},
	OFF_HAND {
		@Override
		public @NotNull ItemStack getItem(Player player) {
			return player.itemInHand(HandTypes.OFF_HAND);
		}
	},
	HELMET {
		@Override
		public @NotNull ItemStack getItem(Player player) {
			return player.head();
		}
	},
	CHESTPLATE {
		@Override
		public @NotNull ItemStack getItem(Player player) {
			return player.chest();
		}
	},
	LEGGINGS {
		@Override
		public @NotNull ItemStack getItem(Player player) {
			return player.legs();
		}
	},
	BOOTS {
		@Override
		public @NotNull ItemStack getItem(Player player) {
			return player.feet();
		}
	};

	@NotNull
	public abstract ItemStack getItem(Player player);
}
