package dev.qixils.crowdcontrol.plugin.mojmap.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.mojmap.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static dev.qixils.crowdcontrol.common.CommandConstants.ENTITY_ARMOR_INC;
import static dev.qixils.crowdcontrol.common.CommandConstants.ENTITY_ARMOR_START;
import static dev.qixils.crowdcontrol.plugin.mojmap.MojmapPlugin.VIEWER_SPAWNED;

@Getter
public class SummonEntityCommand<E extends Entity> extends ImmediateCommand {
	private final Map<EquipmentSlot, List<Item>> armor;
	protected final EntityType<E> entityType;
	protected final boolean isMonster;
	private final String effectName;
	private final String displayName;

	public SummonEntityCommand(MojmapPlugin<?> plugin, EntityType<E> entityType) {
		super(plugin);
		this.entityType = entityType;
		this.isMonster = entityType.getCategory() == MobCategory.MONSTER;
		this.effectName = "entity_" + Registry.ENTITY_TYPE.getKey(entityType).getPath();
		this.displayName = "Summon " + plugin.getTextUtil().asPlain(entityType.getDescription());

		// pre-compute the map of valid armor pieces
		Map<EquipmentSlot, List<Item>> armor = new HashMap<>(4);
		for (Item item : Registry.ITEM) {
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
			for (ServerLevel world : plugin.server().getAllLevels()) {
				if (world.getDifficulty() == Difficulty.PEACEFUL) {
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
	protected E spawnEntity(String viewer, ServerPlayer player) {
		E entity = entityType.create(player.level);
		if (entity == null)
			throw new IllegalStateException("Could not spawn entity");
		// set variables
		entity.setPos(player.position());
		entity.setCustomName(new TextComponent(viewer));
		entity.setCustomNameVisible(true);
		if (entity instanceof TamableAnimal tamable)
			tamable.tame(player);
		entity.getEntityData().set(VIEWER_SPAWNED, true);
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