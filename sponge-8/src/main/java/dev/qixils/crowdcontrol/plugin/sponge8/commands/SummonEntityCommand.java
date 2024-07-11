package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.ExecuteUsing;
import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.MooshroomTypes;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentGroups;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.*;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.common.util.RandomUtil.*;

@ExecuteUsing(ExecuteUsing.Type.SYNC_GLOBAL)
@Getter
public class SummonEntityCommand<E extends Entity> extends ImmediateCommand implements EntityCommand<E> {
	private final Map<EquipmentType, List<ItemType>> armor;
	protected final EntityType<E> entityType;
	private final String effectName;
	private final Component displayName;
	private final Map<RabbitType, Integer> rabbitVariants;
	private final Set<EquipmentType> hands;

	public SummonEntityCommand(SpongeCrowdControlPlugin plugin, EntityType<E> entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "entity_" + csIdOf(entityType.key(RegistryTypes.ENTITY_TYPE));
		this.displayName = Component.translatable("cc.effect.summon_entity.name", entityType);

		// pre-compute rabbits
		List<RabbitType> allVariants = plugin.registryList(RegistryTypes.RABBIT_TYPE);
		Map<RabbitType, Integer> weightedVariants = new HashMap<>();
		int totalVariants = allVariants.size() - 1; // subtract 1 for killer rabbit
		int weight = 100 / totalVariants;
		for (RabbitType variant : allVariants) {
			ResourceKey key = variant.key(RegistryTypes.RABBIT_TYPE);
			if (key.value().equals("evil") || key.value().equals("killer"))
				weightedVariants.put(variant, 1);
			else
				weightedVariants.put(variant, weight);
		}
		rabbitVariants = Collections.unmodifiableMap(weightedVariants);

		// pre-compute the map of valid armor pieces
		Map<EquipmentType, List<ItemType>> armor = new HashMap<>(4);
		for (ItemType item : plugin.registryIterable(RegistryTypes.ITEM_TYPE)) {
			Optional<EquipmentType> optionalType = ItemStack.of(item).get(Keys.EQUIPMENT_TYPE);
			if (!optionalType.isPresent()) continue;

			EquipmentType type = optionalType.get();
			if (EquipmentGroups.HELD.get().equals(type.group())) continue;

			armor.computeIfAbsent(type, $ -> new ArrayList<>()).add(item);
		}

		// make collections unmodifiable
		for (Map.Entry<EquipmentType, List<ItemType>> entry : new HashSet<>(armor.entrySet()))
			armor.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
		this.armor = Collections.unmodifiableMap(armor);

		this.hands = plugin.registryList(RegistryTypes.EQUIPMENT_TYPE).stream().filter(type -> type.group().equals(EquipmentGroups.HELD.get())).collect(Collectors.toSet());
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder tryExecute = tryExecute(players, request);
		if (tryExecute != null) return tryExecute;

		Component name = plugin.getViewerComponentOrNull(request, false);

		LimitConfig config = getPlugin().getLimitConfig();
		int maxVictims = config.getEntityLimit(entityType.key(RegistryTypes.ENTITY_TYPE).value());
		int victims = 0;

		// first pass (hosts)
		for (ServerPlayer player : players) {
			if (!config.hostsBypass() && maxVictims > 0 && victims >= maxVictims)
				break;
			if (!isHost(player))
				continue;

			try {
				spawnEntity(name, player);
				victims++;
			} catch (Exception e) {
				plugin.getSLF4JLogger().error("Failed to spawn entity", e);
			}
		}

		// second pass (guests)
		for (ServerPlayer player : players) {
			if (maxVictims > 0 && victims >= maxVictims)
				break;
			if (isHost(player))
				continue;

			try {
				spawnEntity(name, player);
				victims++;
			} catch (Exception e) {
				plugin.getSLF4JLogger().error("Failed to spawn entity", e);
			}
		}

		return victims > 0
			? request.buildResponse().type(ResultType.SUCCESS)
			: request.buildResponse().type(ResultType.UNAVAILABLE).message("Failed to spawn entity");
	}

	@Blocking
	protected Entity spawnEntity(@Nullable Component viewer, @NotNull ServerPlayer player) {
		Entity entity = player.world().createEntity(entityType, player.position());
		// set variables
		if (viewer != null) {
			entity.offer(Keys.CUSTOM_NAME, viewer);
			entity.offer(Keys.IS_CUSTOM_NAME_VISIBLE, true);
		}
		entity.offer(Keys.IS_TAMED, true);
		entity.offer(Keys.TAMER, player.uniqueId());
		entity.offer(Keys.BOAT_TYPE, randomElementFrom(plugin.registryIterator(RegistryTypes.BOAT_TYPE)));
		entity.offer(Keys.DYE_COLOR, randomElementFrom(plugin.registryIterator(RegistryTypes.DYE_COLOR)));
		if (RNG.nextDouble() < MUSHROOM_COW_BROWN_CHANCE)
			entity.offer(Keys.MOOSHROOM_TYPE, MooshroomTypes.BROWN.get());
		entity.offer(Keys.IS_SADDLED, RNG.nextBoolean());
		entity.offer(Keys.HAS_CHEST, RNG.nextBoolean());
		// TODO horse armor
		// enderman held block API is unavailable in API v8 | todo: open issue?
		entity.offer(Keys.RABBIT_TYPE, weightedRandom(rabbitVariants));
		entity.offer(Keys.VILLAGER_TYPE, randomElementFrom(plugin.registryIterator(RegistryTypes.VILLAGER_TYPE)));
		entity.offer(SpongeCrowdControlPlugin.VIEWER_SPAWNED, true);
		// API8: loot table data | TODO: still no API for it; may need to open an issue

		// add random armor to armor stands
		if (entity instanceof ArmorStand) {
			// could add some chaos (GH#64) here eventually
			// chaos idea: set drop chance for each slot to a random float
			//             (not that this is in API v7 afaik)
			ArmorStand armorStand = (ArmorStand) entity;
			List<EquipmentType> slots = new ArrayList<>(armor.keySet());
			Collections.shuffle(slots, random);
			// begins as a 1 in 4 chance to add a random item but becomes less likely each time
			// an item is added
			int odds = ENTITY_ARMOR_START;
			for (EquipmentType type : slots) {
				if (random.nextInt(odds) > 0)
					continue;
				odds += ENTITY_ARMOR_INC;
				ItemStack item = ItemStack.of(randomElementFrom(armor.get(type)));
				plugin.commandRegister()
						.getCommandByName("lootbox", LootboxCommand.class)
						.randomlyModifyItem(item, odds / ENTITY_ARMOR_START);
				armorStand.equip(type, item);
			}

			if (RNG.nextBoolean()) {
				entity.offer(Keys.HAS_ARMS, true);
				for (EquipmentType slot : hands) {
					if (!RNG.nextBoolean()) continue;
					armorStand.equip(slot, plugin.commandRegister().getCommandByName("lootbox", LootboxCommand.class).createRandomItem(RNG.nextInt(6)));
				}
			}
		}

		// spawn entity
		try (StackFrame frame = plugin.getGame().server().causeStackManager().pushCauseFrame()) {
			frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
			player.world().spawnEntity(entity);
		}
		return entity;
	}
}
