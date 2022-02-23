package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.HeldEquipmentType;
import org.spongepowered.api.item.inventory.property.EquipmentSlotType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.difficulty.Difficulties;

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
	protected final EntityType entityType;
	private final String effectName;
	private final String displayName;

	public SummonEntityCommand(SpongeCrowdControlPlugin plugin, EntityType entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "entity_" + SpongeTextUtil.csIdOf(entityType);
		this.displayName = "Summon " + SpongeTextUtil.getFixedName(entityType);

		// pre-compute the map of valid armor pieces
		Map<EquipmentType, List<ItemType>> armor = new HashMap<>(4);
		for (ItemType item : plugin.getRegistry().getAllOf(ItemType.class)) {
			Optional<EquipmentType> optionalType = item
					.getDefaultProperty(EquipmentSlotType.class)
					.map(EquipmentSlotType::getValue);
			if (!optionalType.isPresent()) continue;

			EquipmentType type = optionalType.get();
			if (type instanceof HeldEquipmentType)
				continue;

			armor.computeIfAbsent(type, $ -> new ArrayList<>()).add(item);
		}

		// make collections unmodifiable
		for (Map.Entry<EquipmentType, List<ItemType>> entry : new HashSet<>(armor.entrySet()))
			armor.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
		this.armor = Collections.unmodifiableMap(armor);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Class<? extends Entity> entityClass = entityType.getEntityClass();
		if (Monster.class.isAssignableFrom(entityClass)) {
			for (World world : plugin.getGame().getServer().getWorlds()) {
				if (world.getDifficulty().equals(Difficulties.PEACEFUL)) {
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
	protected Entity spawnEntity(String viewer, Player player) {
		Entity entity = player.getLocation().createEntity(entityType);
		// set variables
		entity.offer(Keys.DISPLAY_NAME, Text.of(viewer));
		entity.offer(Keys.CUSTOM_NAME_VISIBLE, true);
		entity.offer(Keys.TAMED_OWNER, Optional.of(player.getUniqueId()));
		entity.offer(SpongeCrowdControlPlugin.VIEWER_SPAWNED, true);

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
				plugin.getRegister().getCommandByName("lootbox", LootboxCommand.class)
						.randomlyModifyItem(item, odds / ENTITY_ARMOR_START);
				((ArmorEquipable) entity).equip(type, item);
			}
		}

		// spawn entity
		try (StackFrame frame = plugin.getGame().getCauseStackManager().pushCauseFrame()) {
			frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLUGIN);
			player.getWorld().spawnEntity(entity);
		}
		return entity;
	}
}
