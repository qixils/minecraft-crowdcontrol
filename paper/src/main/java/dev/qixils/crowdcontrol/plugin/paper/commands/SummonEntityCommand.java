package dev.qixils.crowdcontrol.plugin.paper.commands;

import com.destroystokyo.paper.loottable.LootableInventory;
import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.paper.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_ARMOR_INC;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_ARMOR_START;

@Getter
public class SummonEntityCommand extends ImmediateCommand {
	private static final Map<EquipmentSlot, List<Material>> ARMOR;
	private static final Set<LootTables> CHEST_LOOT_TABLES;

	static {
		// --- equipment --- //

		// pre-compute the map of valid armor pieces
		Map<EquipmentSlot, List<Material>> armor = new EnumMap<>(EquipmentSlot.class);
		for (Material material : Material.values()) {
			if (!material.isItem()) continue;
			EquipmentSlot slot = material.getEquipmentSlot();
			if (slot == EquipmentSlot.HAND) continue;
			if (slot == EquipmentSlot.OFF_HAND) continue;
			armor.computeIfAbsent(slot, $ -> new ArrayList<>()).add(material);
		}

		// make collections unmodifiable
		for (Map.Entry<EquipmentSlot, List<Material>> entry : Set.copyOf(armor.entrySet()))
			armor.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
		ARMOR = Collections.unmodifiableMap(armor);

		// --- loot tables --- //
		EnumSet<LootTables> lootTables = EnumSet.noneOf(LootTables.class);
		for (LootTables lootTable : LootTables.values()) {
			String key = lootTable.getKey().getKey();
			if (key.startsWith("chests/"))
				lootTables.add(lootTable);
		}
		CHEST_LOOT_TABLES = Collections.unmodifiableSet(lootTables);
	}

	protected final EntityType entityType;
	private final String effectName;
	private final Component displayName;
	private final NamespacedKey mobKey;

	public SummonEntityCommand(PaperCrowdControlPlugin plugin, EntityType entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "entity_" + entityType.name();
		this.displayName = Component.translatable("cc.effect.summon_entity.name", Component.translatable(entityType));
		this.mobKey = getMobKey(plugin);
	}

	@NotNull
	protected static NamespacedKey getMobKey(Plugin plugin) {
		return new NamespacedKey(plugin, "isViewerSpawned");
	}

	public static boolean isMobViewerSpawned(Plugin plugin, Entity entity) {
		return entity.getPersistentDataContainer().getOrDefault(getMobKey(plugin), PaperCrowdControlPlugin.BOOLEAN_TYPE, false);
	}

	@Override
	public Response.@NotNull Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (entityType.getEntityClass() != null && Monster.class.isAssignableFrom(entityType.getEntityClass())) {
			for (World world : Bukkit.getWorlds()) {
				if (world.getDifficulty() == Difficulty.PEACEFUL)
					return request.buildResponse().type(Response.ResultType.FAILURE).message("Hostile mobs cannot be spawned while on Peaceful difficulty");
			}
		}

		LimitConfig config = plugin.getLimitConfig();
		int maxVictims = config.getEntityLimit(entityType.getKey().getKey());

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
		return request.buildResponse().type(Response.ResultType.SUCCESS);
	}

	protected Entity spawnEntity(@NotNull Component viewer, @NotNull Player player) {
		Entity entity = player.getWorld().spawnEntity(player.getLocation(), entityType);
		entity.customName(viewer);
		entity.setCustomNameVisible(true);

		if (entity instanceof Tameable tameable)
			tameable.setOwner(player);

		if (entity instanceof LootableInventory lootable)
			lootable.setLootTable(RandomUtil.randomElementFrom(CHEST_LOOT_TABLES).getLootTable());

		if (entity instanceof ArmorStand) {
			// could add some chaos (GH#64) here eventually
			// chaos idea: set drop chance for each slot to a random float
			EntityEquipment equipment = ((LivingEntity) entity).getEquipment();
			if (equipment != null) {
				List<EquipmentSlot> slots = new ArrayList<>(ARMOR.keySet());
				Collections.shuffle(slots, random);
				// begins as a 1 in 4 chance to add a random item but becomes less likely each time
				// an item is added
				int odds = ENTITY_ARMOR_START;
				for (EquipmentSlot slot : slots) {
					if (random.nextInt(odds) > 0)
						continue;
					odds += ENTITY_ARMOR_INC;
					ItemStack item = new ItemStack(RandomUtil.randomElementFrom(ARMOR.get(slot)));
					LootboxCommand.randomlyModifyItem(item, odds / ENTITY_ARMOR_START);
					equipment.setItem(slot, item, true);
				}
			}
		}

		entity.getPersistentDataContainer().set(mobKey, PaperCrowdControlPlugin.BOOLEAN_TYPE, true);
		return entity;
	}
}
