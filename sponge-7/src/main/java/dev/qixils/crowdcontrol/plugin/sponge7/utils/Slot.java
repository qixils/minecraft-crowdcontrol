package dev.qixils.crowdcontrol.plugin.sponge7.utils;

import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;

public enum Slot {
	MAIN_HAND {
		@Override
		public Optional<ItemStack> getItem(Player player) {
			return player.getItemInHand(HandTypes.MAIN_HAND);
		}
	},
	OFF_HAND {
		@Override
		public Optional<ItemStack> getItem(Player player) {
			return player.getItemInHand(HandTypes.OFF_HAND);
		}
	},
	HELMET {
		@Override
		public Optional<ItemStack> getItem(Player player) {
			return player.getHelmet();
		}
	},
	CHESTPLATE {
		@Override
		public Optional<ItemStack> getItem(Player player) {
			return player.getChestplate();
		}
	},
	LEGGINGS {
		@Override
		public Optional<ItemStack> getItem(Player player) {
			return player.getLeggings();
		}
	},
	BOOTS {
		@Override
		public Optional<ItemStack> getItem(Player player) {
			return player.getBoots();
		}
	};

	public abstract Optional<ItemStack> getItem(Player player);
}
