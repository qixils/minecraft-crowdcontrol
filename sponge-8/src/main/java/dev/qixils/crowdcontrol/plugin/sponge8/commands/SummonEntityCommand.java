package dev.qixils.crowdcontrol.plugin.sponge8.commands;

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
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.qixils.crowdcontrol.common.CommandConstants.ENTITY_ARMOR_INC;
import static dev.qixils.crowdcontrol.common.CommandConstants.ENTITY_ARMOR_START;

@Getter
public class SummonEntityCommand extends ImmediateCommand {
	private final Map<EquipmentType, List<ItemType>> armor;
	protected final EntityType<?> entityType;
	protected final boolean isMonster;
	private final String effectName;
	private final String displayName;

	public SummonEntityCommand(SpongeCrowdControlPlugin plugin, EntityType<?> entityType) {
		super(plugin);
		this.entityType = entityType;
		this.isMonster = entityType.category().equals(EntityCategories.MONSTER.get());
		this.effectName = "entity_" + entityType.key(RegistryTypes.ENTITY_TYPE).value();
		this.displayName = "Summon " + plugin.getTextUtil().asPlain(entityType);

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
			for (ServerWorld world : plugin.getGame().server().worldManager().worlds()) {
				if (world.difficulty().equals(Difficulties.PEACEFUL.get())) {
					return request.buildResponse()
							.type(ResultType.FAILURE)
							.message("Hostile mobs cannot be spawned while on Peaceful difficulty");
				}
			}
		}

		sync(() -> players.forEach(player -> spawnEntity(request.getViewer(), player)));
		return request.buildResponse().type(ResultType.SUCCESS);
	}

	@Blocking
	protected Entity spawnEntity(String viewer, ServerPlayer player) {
		Entity entity = player.world().createEntity(entityType, player.position());
		// set variables
		entity.offer(Keys.CUSTOM_NAME, Component.text(viewer));
		entity.offer(Keys.IS_CUSTOM_NAME_VISIBLE, true);
		entity.offer(Keys.IS_TAMED, true);
		entity.offer(Keys.TAMER, player.uniqueId());
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
