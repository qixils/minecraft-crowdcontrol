package dev.qixils.crowdcontrol.plugin.paper.commands;

import com.destroystokyo.paper.MaterialTags;
import com.destroystokyo.paper.loottable.LootableInventory;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import io.papermc.paper.entity.CollarColorable;
import io.papermc.paper.event.player.PlayerNameEntityEvent;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.common.util.RandomUtil.*;

@Getter
public class SummonEntityCommand extends RegionalCommandSync implements EntityCommand, Listener {
	private static final Set<EquipmentSlot> HANDS = Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
	private static final Map<EquipmentSlot, List<Material>> ARMOR;
	private static final Set<LootTables> CHEST_LOOT_TABLES;
	private static final Set<Material> BLOCKS;
	private static final Map<Rabbit.Type, Integer> RABBIT_VARIANTS = Map.of(
			Rabbit.Type.BLACK, 16,
			Rabbit.Type.BROWN, 16,
			Rabbit.Type.GOLD, 16,
			Rabbit.Type.SALT_AND_PEPPER, 16,
			Rabbit.Type.WHITE, 16,
			Rabbit.Type.BLACK_AND_WHITE, 16,
			Rabbit.Type.THE_KILLER_BUNNY, 1
	);

	static {
		// --- equipment --- //

		// pre-compute the map of valid armor pieces
		Map<EquipmentSlot, List<Material>> armor = new EnumMap<>(EquipmentSlot.class);
		for (Material material : Registry.MATERIAL) {
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
		for (LootTables lootTable : Registry.LOOT_TABLES) {
			String key = lootTable.getKey().getKey();
			if (key.startsWith("chests/"))
				lootTables.add(lootTable);
		}
		CHEST_LOOT_TABLES = Collections.unmodifiableSet(lootTables);

		// --- blocks --- //
		Set<Material> blocks = new HashSet<>();
		for (Material material : Registry.MATERIAL) {
			if (material.isBlock())
				blocks.add(material);
		}
		BLOCKS = Collections.unmodifiableSet(blocks);
	}

	protected final EntityType entityType;
	private final String effectName;
	private final Component displayName;
	private final NamespacedKey mobKey;

	public SummonEntityCommand(PaperCrowdControlPlugin plugin, EntityType entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "entity_" + csIdOf(entityType);
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
	protected int getPlayerLimit() {
		return getPlugin().getLimitConfig().getEntityLimit(entityType.getKey().getKey());
	}

	@Override
	protected Response.@NotNull Builder buildFailure(@NotNull Request request) {
		return request.buildResponse()
			.type(Response.ResultType.UNAVAILABLE)
			.message("Failed to spawn entity; likely not supported by this version of Minecraft");
	}

	@Override
	protected Response.@Nullable Builder precheck(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		return tryExecute(players, request); // todo: a bit redundant?
	}

	@Override
	protected boolean executeRegionallySync(@NotNull Player player, @NotNull Request request) {
		spawnEntity(plugin.getViewerComponentOrNull(request, false), player);
		return true;
	}

	protected Entity spawnEntity(@Nullable Component viewer, @NotNull Player player) {
		Entity entity = player.getWorld().spawnEntity(player.getLocation(), entityType);
		if (viewer != null) {
			entity.customName(viewer);
			entity.setCustomNameVisible(true);
		}
		if (entity instanceof Tameable tameable)
			tameable.setOwner(player);
		if (entity instanceof Boat boat)
			boat.setBoatType(randomElementFrom(Boat.Type.class));
		if (entity instanceof CollarColorable colorable)
			colorable.setCollarColor(randomElementFrom(DyeColor.class));
		if (entity instanceof MushroomCow mooshroom && RNG.nextDouble() < MUSHROOM_COW_BROWN_CHANCE)
			mooshroom.setVariant(MushroomCow.Variant.BROWN);
		if (entity instanceof Horse horse && RNG.nextBoolean())
			horse.getInventory().setArmor(new ItemStack(randomElementFrom(MaterialTags.HORSE_ARMORS.getValues())));
		if (entity instanceof Llama llama && RNG.nextBoolean())
			llama.getInventory().setDecor(new ItemStack(randomElementFrom(Tag.WOOL_CARPETS.getValues())));
		if (entity instanceof Sheep sheep)
			sheep.setColor(randomElementFrom(DyeColor.class));
		if (entity instanceof Steerable steerable)
			steerable.setSaddle(RNG.nextBoolean());
		if (entity instanceof Enderman enderman)
			enderman.setCarriedBlock(randomElementFrom(BLOCKS).createBlockData());
		if (entity instanceof ChestedHorse horse)
			horse.setCarryingChest(RNG.nextBoolean());
		if (entity instanceof Frog frog)
			frog.setVariant(randomElementFrom(Frog.Variant.class));
		if (entity instanceof Axolotl axolotl)
			axolotl.setVariant(randomElementFrom(Axolotl.Variant.class));
		if (entity instanceof Rabbit rabbit)
			rabbit.setRabbitType(weightedRandom(RABBIT_VARIANTS));
		if (entity instanceof Villager villager)
			villager.setVillagerType(randomElementFrom(Villager.Type.class));
		if (entity instanceof ZombieVillager villager)
			villager.setVillagerType(randomElementFrom(Villager.Type.class));
		if (entity instanceof LootableInventory lootable)
			lootable.setLootTable(randomElementFrom(CHEST_LOOT_TABLES).getLootTable());

		if (entity instanceof ArmorStand armorStand) {
			// could add some chaos (GH#64) here eventually
			// chaos idea: set drop chance for each slot to a random float
			EntityEquipment equipment = armorStand.getEquipment();
            List<EquipmentSlot> slots = new ArrayList<>(ARMOR.keySet());
            Collections.shuffle(slots, random);
            // begins as a 1 in 4 chance to add a random item but becomes less likely each time
            // an item is added
            int odds = ENTITY_ARMOR_START;
            for (EquipmentSlot slot : slots) {
                if (random.nextInt(odds) > 0)
                    continue;
                odds += ENTITY_ARMOR_INC;
                ItemStack item = new ItemStack(randomElementFrom(ARMOR.get(slot)));
                LootboxCommand.randomlyModifyItem(item, odds / ENTITY_ARMOR_START, null);
                equipment.setItem(slot, item, true);
            }

			if (RNG.nextBoolean()) {
				armorStand.setArms(true);
				for (EquipmentSlot slot : HANDS) {
					if (!RNG.nextBoolean()) continue;
					equipment.setItem(slot, LootboxCommand.createRandomItem(RNG.nextInt(6), null), true);
				}
			}
        }

		entity.getPersistentDataContainer().set(mobKey, PaperCrowdControlPlugin.BOOLEAN_TYPE, true);
		return entity;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onNameTag(PlayerNameEntityEvent event) {
		LivingEntity entity = event.getEntity();
		if (!isMobViewerSpawned(plugin, entity)) return;
		entity.getPersistentDataContainer().remove(getMobKey());
	}
}
