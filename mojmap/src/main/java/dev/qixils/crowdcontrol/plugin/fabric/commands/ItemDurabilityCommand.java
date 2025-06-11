package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.command.CommandConstants;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.ITEM_DAMAGE_PERCENTAGE;

@Getter
public abstract class ItemDurabilityCommand extends ModdedCommand {
	private final String effectName;

	protected ItemDurabilityCommand(ModdedCrowdControlPlugin plugin, String effectName) {
		super(plugin);
		this.effectName = effectName;
	}

	protected abstract int modifyDurability(int curDamage, int maxDamage);

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			// create list of random equipment slots
			List<EquipmentSlot> slots = Arrays.asList(EquipmentSlot.values());
			Collections.shuffle(slots);

			boolean success = false;

			// loop through all players and all slots, and apply the durability change
			for (ServerPlayer player : playerSupplier.get()) {
				for (EquipmentSlot slot : slots) {
					ItemStack item = player.getItemBySlot(slot);
					if (item.isEmpty())
						continue;
					if (!item.isDamageableItem())
						continue;
					int curDamage = item.getDamageValue();
					int maxDamage = item.getMaxDamage();
					int newDamage = Math.min(maxDamage, Math.max(0, modifyDurability(curDamage, maxDamage)));

					if (!CommandConstants.canApplyDamage(curDamage, newDamage, maxDamage))
						continue;

					success = true;
					item.setDamageValue(newDamage);
					break;
				}
			}

			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_TEMPORARY, "Targets not holding a durable item");
		}));
	}

	// repair command
	@NotNull
	public static final class Repair extends ItemDurabilityCommand {
		public Repair(ModdedCrowdControlPlugin plugin) {
			super(plugin, "repair_item");
		}

		@Override
		protected int modifyDurability(int curDamage, int maxDamage) {
			return 0;
		}
	}

	// damage command (cuts the usable durability in half)
	@NotNull
	public static final class Damage extends ItemDurabilityCommand {
		public Damage(ModdedCrowdControlPlugin plugin) {
			super(plugin, "damage_item");
		}

		@Override
		protected int modifyDurability(int curDamage, int maxDamage) {
			return curDamage + (maxDamage / ITEM_DAMAGE_PERCENTAGE);
		}
	}
}
