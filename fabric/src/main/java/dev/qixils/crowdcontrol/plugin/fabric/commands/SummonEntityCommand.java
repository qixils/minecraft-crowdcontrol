package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.fabric.FabricCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.fabric.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.fabric.interfaces.Components;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.text.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_ARMOR_INC;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_ARMOR_START;

@Getter
public class SummonEntityCommand<E extends Entity> extends ImmediateCommand {
	private final Map<EquipmentSlot, List<Item>> armor;
	protected final EntityType<E> entityType;
	protected final boolean isMonster;
	private final String effectName;
	private final Component displayName;

	public SummonEntityCommand(FabricCrowdControlPlugin plugin, EntityType<E> entityType) {
		super(plugin);
		this.entityType = entityType;
		this.isMonster = entityType.getCategory() == MobCategory.MONSTER;
		this.effectName = "entity_" + BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getPath();
		this.displayName = Component.translatable("cc.effect.summon_entity.name", entityType.getDescription());

		// pre-compute the map of valid armor pieces
		Map<EquipmentSlot, List<Item>> armor = new HashMap<>(4);
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof ArmorItem armorItem) {
				EquipmentSlot slot = armorItem.getSlot();
				if (slot.getType() != EquipmentSlot.Type.ARMOR)
					continue;
				armor.computeIfAbsent(slot, $ -> new ArrayList<>()).add(armorItem);
			}
		}

		// make collections unmodifiable
		for (Map.Entry<EquipmentSlot, List<Item>> entry : new HashSet<>(armor.entrySet()))
			armor.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
		this.armor = Collections.unmodifiableMap(armor);
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		if (isMonster) {
			for (ServerPlayer player : players) {
				if (player.getLevel().getDifficulty() == Difficulty.PEACEFUL) {
					return request.buildResponse()
							.type(ResultType.FAILURE)
							.message("Hostile mobs cannot be spawned while on Peaceful difficulty");
				}
			}
		}

		LimitConfig config = getPlugin().getLimitConfig();
		int maxVictims = config.getItemLimit(BuiltInRegistries.ENTITY_TYPE.getKey(entityType).getPath());

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
	protected E spawnEntity(@NotNull Component viewer, @NotNull ServerPlayer player) {
		E entity = entityType.create(player.level);
		if (entity == null)
			throw new IllegalStateException("Could not spawn entity");
		// set variables
		entity.setPos(player.position());
		entity.setCustomName(FabricAudiences.nonWrappingSerializer().serialize(plugin.adventure().renderer().render(viewer, player)));
		entity.setCustomNameVisible(true);
		if (entity instanceof TamableAnimal tamable)
			tamable.tame(player);
		if (entity instanceof LivingEntity)
			Components.VIEWER_MOB.get(entity).setViewerSpawned();
		if (entity instanceof Boat boat)
			boat.setVariant(RandomUtil.randomElementFrom(Boat.Type.class));
		// TODO: random loot table data

		// add random armor to armor stands
		if (entity instanceof ArmorStand) {
			// could add some chaos (GH#64) here eventually
			// chaos idea: set drop chance for each slot to a random float
			//             (not that this is in API v7 afaik)
			List<EquipmentSlot> slots = new ArrayList<>(armor.keySet());
			Collections.shuffle(slots, random);
			// begins as a 1 in 4 chance to add a random item but becomes less likely each time
			// an item is added
			int odds = ENTITY_ARMOR_START;
			for (EquipmentSlot type : slots) {
				if (random.nextInt(odds) > 0)
					continue;
				odds += ENTITY_ARMOR_INC;
				ItemStack item = new ItemStack(RandomUtil.randomElementFrom(armor.get(type)));
				plugin.commandRegister()
						.getCommandByName("lootbox", LootboxCommand.class)
						.randomlyModifyItem(item, odds / ENTITY_ARMOR_START);
				entity.setItemSlot(type, item);
			}
		}

		// spawn entity
		player.level.addFreshEntity(entity);
		return entity;
	}
}
