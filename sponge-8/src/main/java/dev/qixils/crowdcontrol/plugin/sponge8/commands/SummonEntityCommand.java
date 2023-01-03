package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.MooshroomTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityCategories;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ArmorEquipable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentGroups;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.difficulty.Difficulties;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;

@Getter
public class SummonEntityCommand extends ImmediateCommand {
	private final Map<EquipmentType, List<ItemType>> armor;
	protected final EntityType<?> entityType;
	protected final boolean isMonster;
	private final String effectName;
	private final Component displayName;

	public SummonEntityCommand(SpongeCrowdControlPlugin plugin, EntityType<?> entityType) {
		super(plugin);
		this.entityType = entityType;
		this.isMonster = entityType.category().equals(EntityCategories.MONSTER.get());
		this.effectName = "entity_" + entityType.key(RegistryTypes.ENTITY_TYPE).value();
		this.displayName = Component.translatable("cc.effect.summon_entity.name", entityType);

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
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (isMonster) {
			for (ServerPlayer player : players) {
				if (player.world().difficulty().equals(Difficulties.PEACEFUL.get())) {
					return request.buildResponse()
							.type(ResultType.FAILURE)
							.message("Hostile mobs cannot be spawned while on Peaceful difficulty");
				}
			}
		}

		LimitConfig config = getPlugin().getLimitConfig();
		int maxVictims = config.getItemLimit(entityType.key(RegistryTypes.ENTITY_TYPE).value());

		sync(() -> {
			int victims = 0;

			// first pass (hosts)
			for (ServerPlayer player : players) {
				if (!config.hostsBypass() && maxVictims > 0 && victims >= maxVictims)
					break;
				if (!isHost(player))
					continue;

				spawnEntity(plugin.getViewerComponent(request, false), player);
				victims++;
			}

			// second pass (guests)
			for (ServerPlayer player : players) {
				if (maxVictims > 0 && victims >= maxVictims)
					break;
				if (isHost(player))
					continue;

				spawnEntity(plugin.getViewerComponent(request, false), player);
				victims++;
			}
		});
		return request.buildResponse().type(ResultType.SUCCESS);
	}

	@Blocking
	protected Entity spawnEntity(@NotNull Component viewer, @NotNull ServerPlayer player) {
		Entity entity = player.world().createEntity(entityType, player.position());
		// set variables
		entity.offer(Keys.CUSTOM_NAME, viewer);
		entity.offer(Keys.IS_CUSTOM_NAME_VISIBLE, true);
		entity.offer(Keys.IS_TAMED, true);
		entity.offer(Keys.TAMER, player.uniqueId());
		entity.offer(Keys.BOAT_TYPE, RandomUtil.randomElementFrom(plugin.registryIterator(RegistryTypes.BOAT_TYPE)));
		entity.offer(Keys.CAT_TYPE, RandomUtil.randomElementFrom(plugin.registryIterator(RegistryTypes.CAT_TYPE)));
		entity.offer(Keys.DYE_COLOR, RandomUtil.randomElementFrom(plugin.registryIterator(RegistryTypes.DYE_COLOR)));
		if (RandomUtil.RNG.nextDouble() < MUSHROOM_COW_BROWN_CHANCE)
			entity.offer(Keys.MOOSHROOM_TYPE, MooshroomTypes.BROWN.get());
		entity.offer(Keys.HORSE_COLOR, RandomUtil.randomElementFrom(plugin.registryIterator(RegistryTypes.HORSE_COLOR)));
		entity.offer(Keys.HORSE_STYLE, RandomUtil.randomElementFrom(plugin.registryIterator(RegistryTypes.HORSE_STYLE)));
		entity.offer(Keys.PARROT_TYPE, RandomUtil.randomElementFrom(plugin.registryIterator(RegistryTypes.PARROT_TYPE)));
		entity.offer(SpongeCrowdControlPlugin.VIEWER_SPAWNED, true);
		// API8: loot table data | TODO: still no API for it; may need to open an issue

		// add random armor to armor stands
		if (entity instanceof ArmorStand) {
			// could add some chaos (GH#64) here eventually
			// chaos idea: set drop chance for each slot to a random float
			//             (not that this is in API v7 afaik)
			List<EquipmentType> slots = new ArrayList<>(armor.keySet());
			Collections.shuffle(slots, random);
			// begins as a 1 in 4 chance to add a random item but becomes less likely each time
			// an item is added
			int odds = ENTITY_ARMOR_START;
			for (EquipmentType type : slots) {
				if (random.nextInt(odds) > 0)
					continue;
				odds += ENTITY_ARMOR_INC;
				ItemStack item = ItemStack.of(RandomUtil.randomElementFrom(armor.get(type)));
				plugin.commandRegister()
						.getCommandByName("lootbox", LootboxCommand.class)
						.randomlyModifyItem(item, odds / ENTITY_ARMOR_START);
				((ArmorEquipable) entity).equip(type, item);
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
