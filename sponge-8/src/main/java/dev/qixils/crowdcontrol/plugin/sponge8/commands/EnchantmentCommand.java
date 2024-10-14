package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge8.utils.Slot;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.csIdOf;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

@Getter
public class EnchantmentCommand extends ImmediateCommand {
	private final Component displayName;
	private final String effectName;
	private final EnchantmentType enchantmentType;
	private final int maxLevel;

	public EnchantmentCommand(SpongeCrowdControlPlugin plugin, EnchantmentType enchantmentType) {
		super(plugin);
		this.enchantmentType = enchantmentType;
		this.maxLevel = enchantmentType.maximumLevel();
		this.effectName = "enchant_" + csIdOf(enchantmentType.key(RegistryTypes.ENCHANTMENT_TYPE));
		Component enchName = enchantmentType.asComponent().color(null);
		enchName = enchName.children(enchName.children().stream().map(comp -> comp.color(null)).collect(Collectors.toList()));
		TranslatableComponent trans = enchName instanceof TranslatableComponent ? (TranslatableComponent) enchName : null;
		this.displayName = Component.translatable(
				"cc.effect.enchant.name",
				trans != null
					? trans.args(text(enchantmentType.maximumLevel()))
					: enchName.append(space()).append(text(enchantmentType.maximumLevel())
		));
	}

	private int getCurrentLevel(ItemStack item) {
		return item.get(Keys.APPLIED_ENCHANTMENTS)
				.flatMap(enchantments -> enchantments.stream()
						.filter(enchantment -> enchantment.type().equals(enchantmentType))
						.findFirst()
				).map(Enchantment::level).orElse(0);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder response = request.buildResponse()
				.type(ResultType.RETRY)
				.message("No items could be enchanted");
		for (ServerPlayer player : players) {
			// get the equipped item that supports this enchantment and has the lowest level of it
			Map<Slot, Integer> levelMap = new HashMap<>(Slot.values().length);
			for (Slot slot : Slot.values()) {
				ItemStack item = slot.getItem(player);
				if (item.isEmpty())
					continue;
				if (!enchantmentType.canBeAppliedToStack(item))
					continue;
				int curLevel = getCurrentLevel(item);
				if (enchantmentType.maximumLevel() == enchantmentType.minimumLevel() && curLevel == enchantmentType.maximumLevel())
					continue;
				if (curLevel == 255)
					continue;
				levelMap.put(slot, curLevel);
			}
			Slot slot = levelMap.entrySet().stream()
					.min(Comparator.comparingInt(Entry::getValue))
					.map(Entry::getKey).orElse(null);
			if (slot == null)
				continue;
			ItemStack item = slot.getItem(player);

			// misc instantiation
			List<Enchantment> enchantments = new ArrayList<>(item.get(Keys.APPLIED_ENCHANTMENTS).orElseGet(Collections::emptyList));
			Iterator<Enchantment> iterator = enchantments.iterator();

			// determine enchantment level
			Enchantment toAdd = Enchantment.of(enchantmentType, maxLevel);
			while (iterator.hasNext()) {
				Enchantment enchantment = iterator.next();
				if (!enchantment.type().equals(enchantmentType))
					continue;
				int curLevel = enchantment.level();
				if (curLevel >= maxLevel)
					toAdd = Enchantment.of(enchantmentType, curLevel + 1);
				iterator.remove();
			}

			// add enchant
			enchantments.add(toAdd);
			item.offer(Keys.APPLIED_ENCHANTMENTS, enchantments);
			response.type(ResultType.SUCCESS).message("SUCCESS");
		}
		return response;
	}

}
