package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.*;
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
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.difficulty.Difficulties;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_ARMOR_INC;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_ARMOR_START;

@Getter
public class SummonEntityCommand extends ImmediateCommand {
	private final Map<EquipmentType, List<ItemType>> armor;
	protected final EntityType entityType;
	private final String effectName;
	private final Component displayName;

	public SummonEntityCommand(SpongeCrowdControlPlugin plugin, EntityType entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "entity_" + SpongeTextUtil.csIdOf(entityType);
		this.displayName = Component.translatable("cc.effect.summon_entity.name", SpongeTextUtil.getFixedName(entityType));

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

		LimitConfig config = getPlugin().getLimitConfig();
		int maxVictims = config.getItemLimit(SpongeTextUtil.csIdOf(entityType));

		sync(() -> {
			int victims = 0;

			// first pass (hosts)
			for (Player player : players) {
				if (!config.hostsBypass() && maxVictims > 0 && victims >= maxVictims)
					break;
				if (!isHost(player))
					continue;
				spawnEntity(plugin.getViewerComponent(request, false), player);
				victims++;
			}

			// second pass (guests)
			for (Player player : players) {
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
	protected Entity spawnEntity(@NotNull Component viewer, @NotNull Player player) {
		Entity entity = player.getLocation().createEntity(entityType);
		// set variables
		entity.offer(Keys.DISPLAY_NAME, plugin.getSpongeSerializer().serialize(viewer));
		entity.offer(Keys.CUSTOM_NAME_VISIBLE, true);
		entity.offer(Keys.TAMED_OWNER, Optional.of(player.getUniqueId()));
		entity.offer(Keys.TREE_TYPE, RandomUtil.randomElementFrom(plugin.getRegistry().getAllOf(TreeType.class)));
		entity.offer(Keys.OCELOT_TYPE, RandomUtil.randomElementFrom(plugin.getRegistry().getAllOf(OcelotType.class)));
		entity.offer(Keys.DYE_COLOR, RandomUtil.randomElementFrom(plugin.getRegistry().getAllOf(DyeColor.class)));
		entity.offer(Keys.HORSE_COLOR, RandomUtil.randomElementFrom(plugin.getRegistry().getAllOf(HorseColor.class)));
		entity.offer(Keys.HORSE_STYLE, RandomUtil.randomElementFrom(plugin.getRegistry().getAllOf(HorseStyle.class)));
		entity.offer(Keys.PARROT_VARIANT, RandomUtil.randomElementFrom(plugin.getRegistry().getAllOf(ParrotVariant.class)));
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
				plugin.commandRegister().getCommandByName("lootbox", LootboxCommand.class)
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
