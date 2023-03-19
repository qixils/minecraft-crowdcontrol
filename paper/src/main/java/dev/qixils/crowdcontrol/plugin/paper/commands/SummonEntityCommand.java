package dev.qixils.crowdcontrol.plugin.paper.commands;

import com.destroystokyo.paper.MaterialTags;
import com.destroystokyo.paper.loottable.LootableInventory;
import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.paper.Command;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.utils.ReflectionUtil;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.common.util.RandomUtil.RNG;
import static dev.qixils.crowdcontrol.common.util.RandomUtil.randomElementFrom;

@Getter
public class SummonEntityCommand extends Command {
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

		// --- blocks --- //
		EnumSet<Material> blocks = EnumSet.noneOf(Material.class);
		for (Material material : Material.values()) {
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
	public @NotNull CompletableFuture<Response.@Nullable Builder> execute(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		if (entityType.getEntityClass() != null && Monster.class.isAssignableFrom(entityType.getEntityClass())) {
			for (World world : Bukkit.getWorlds()) {
				if (world.getDifficulty() == Difficulty.PEACEFUL)
					return CompletableFuture.completedFuture(request.buildResponse()
							.type(Response.ResultType.FAILURE)
							.message("Hostile mobs cannot be spawned while on Peaceful difficulty"));
			}
		}

		CompletableFuture<Response.Builder> future = new CompletableFuture<>();
		LimitConfig config = plugin.getLimitConfig();
		int maxVictims = config.getEntityLimit(entityType.getKey().getKey());

		sync(() -> {
			try {
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
			} catch (Exception e) {
				future.complete(request.buildResponse()
						.type(Response.ResultType.UNAVAILABLE)
						.message("Failed to spawn entity; likely not supported by this version of Minecraft"));
			}
			future.complete(request.buildResponse().type(Response.ResultType.SUCCESS));
		});
		return future;
	}

	protected Entity spawnEntity(@NotNull Component viewer, @NotNull Player player) {
		Entity entity = player.getWorld().spawnEntity(player.getLocation(), entityType);
		entity.customName(viewer);
		entity.setCustomNameVisible(true);
		if (entity instanceof Tameable tameable)
			tameable.setOwner(player);
		if (entity instanceof Boat boat) {
			boolean boatTypeSet = ReflectionUtil.getClazz("org.bukkit.entity.Boat$Type")
					.map(boatTypeClass -> ReflectionUtil.invokeVoidMethod(entity, "setBoatType", randomElementFrom(boatTypeClass.getEnumConstants())))
					.orElse(false);
			if (!boatTypeSet)
				boat.setWoodType(randomElementFrom(TreeSpecies.class));
		}
		ReflectionUtil.invokeVoidMethod(entity, "setCollarColor", randomElementFrom(DyeColor.class));
		if (entity instanceof MushroomCow mooshroom && RNG.nextDouble() < MUSHROOM_COW_BROWN_CHANCE)
			mooshroom.setVariant(MushroomCow.Variant.BROWN);
		if (entity instanceof Horse horse && RNG.nextBoolean())
			horse.getInventory().setArmor(new ItemStack(randomElementFrom(MaterialTags.HORSE_ARMORS.getValues())));
		if (entity instanceof Llama llama && RNG.nextBoolean()) {
			Tag<Material> carpetTag = ReflectionUtil.<Tag<Material>>getFieldValue(null, Tag.class, "WOOL_CARPETS").orElse(Tag.CARPETS);
			llama.getInventory().setDecor(new ItemStack(randomElementFrom(carpetTag.getValues())));
		}
		if (entity instanceof Sheep sheep)
			sheep.setColor(randomElementFrom(DyeColor.class));
		if (entity instanceof Steerable steerable)
			steerable.setSaddle(RNG.nextBoolean());
		if (entity instanceof Enderman enderman)
			enderman.setCarriedBlock(randomElementFrom(BLOCKS).createBlockData());
		if (entity instanceof ChestedHorse horse)
			horse.setCarryingChest(RNG.nextBoolean());
		ReflectionUtil.getClazz("org.bukkit.entity.Frog$Variant").ifPresent(variantClass ->
				ReflectionUtil.invokeVoidMethod(entity, "setVariant", randomElementFrom(variantClass.getEnumConstants())));
		if (entity instanceof Axolotl axolotl)
			axolotl.setVariant(randomElementFrom(Axolotl.Variant.class));
		if (entity instanceof Rabbit rabbit)
			rabbit.setRabbitType(randomElementFrom(Rabbit.Type.class));
		if (entity instanceof Villager villager)
			villager.setVillagerType(randomElementFrom(Villager.Type.class));
		if (entity instanceof ZombieVillager villager)
			villager.setVillagerType(randomElementFrom(Villager.Type.class));
		if (entity instanceof LootableInventory lootable)
			lootable.setLootTable(randomElementFrom(CHEST_LOOT_TABLES).getLootTable());

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
					ItemStack item = new ItemStack(randomElementFrom(ARMOR.get(slot)));
					LootboxCommand.randomlyModifyItem(item, odds / ENTITY_ARMOR_START);
					equipment.setItem(slot, item, true);
				}
			}
		}

		entity.getPersistentDataContainer().set(mobKey, PaperCrowdControlPlugin.BOOLEAN_TYPE, true);
		return entity;
	}
}
