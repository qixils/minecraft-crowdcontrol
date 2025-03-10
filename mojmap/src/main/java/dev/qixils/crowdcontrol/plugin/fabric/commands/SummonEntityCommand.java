package dev.qixils.crowdcontrol.plugin.fabric.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.common.util.RandomUtil.*;

@Getter
public class SummonEntityCommand<E extends Entity> extends ModdedCommand implements EntityCommand<E> {
	private static final Set<EquipmentSlot> HANDS = Arrays.stream(EquipmentSlot.values()).filter(slot -> slot.getType() == EquipmentSlot.Type.HAND).collect(Collectors.toSet());
	private final Map<EquipmentSlot, List<ArmorItem>> humanoidArmor;
	protected final EntityType<? extends E> entityType;
	protected final EntityType<? extends E>[] entityTypes;
	private final String effectName;
	private final Component displayName;
	private List<ResourceKey<LootTable>> lootTables = null;
	private final Map<EntityType<?>, List<Item>> horseArmor = new HashMap<>();
	private static final Map<Rabbit.Variant, Integer> RABBIT_VARIANTS = Map.of(
			Rabbit.Variant.BLACK, 16,
			Rabbit.Variant.BROWN, 16,
			Rabbit.Variant.GOLD, 16,
			Rabbit.Variant.SALT, 16,
			Rabbit.Variant.WHITE, 16,
			Rabbit.Variant.WHITE_SPLOTCHED, 16,
			Rabbit.Variant.EVIL, 1
	);

	public SummonEntityCommand(ModdedCrowdControlPlugin plugin, EntityType<E> entityType) {
		this(
			plugin,
			"entity_" + csIdOf(BuiltInRegistries.ENTITY_TYPE.getKey(entityType)),
			Component.translatable("cc.effect.summon_entity.name", plugin.toAdventure(entityType.getDescription())),
			entityType
		);
	}

	@SafeVarargs
	public SummonEntityCommand(ModdedCrowdControlPlugin plugin, String effectName, @Nullable Component displayName, EntityType<? extends E> firstEntity, EntityType<? extends E>... otherEntities) {
		super(plugin);
		this.entityType = firstEntity;
		this.entityTypes = new EntityType[1 + otherEntities.length];
		this.entityTypes[0] = firstEntity;
		System.arraycopy(otherEntities, 0, this.entityTypes, 1, otherEntities.length);

		this.effectName = effectName;
		this.displayName = displayName;

		// pre-compute the map of valid armor pieces
		Map<EquipmentSlot, List<ArmorItem>> armor = new HashMap<>(4);
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof ArmorItem armorItem) {
				Equippable equippable = armorItem.components().get(DataComponents.EQUIPPABLE);
				if (equippable == null) continue;
				EquipmentSlot slot = equippable.slot();
				if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR)
					continue;
				armor.computeIfAbsent(slot, $ -> new ArrayList<>()).add(armorItem);
			}
		}

		// make collections unmodifiable
		for (Map.Entry<EquipmentSlot, List<ArmorItem>> entry : new HashSet<>(armor.entrySet()))
			armor.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
		this.humanoidArmor = Collections.unmodifiableMap(armor);
	}

	public @NotNull Component getDisplayName() {
		if (displayName != null) return displayName;
		return getDefaultDisplayName();
	}

	public EntityType<? extends E> getRandomEntityType() {
		return RandomUtil.randomElementFrom(entityTypes);
	}

	private List<ResourceKey<LootTable>> getLootTables(MinecraftServer server) {
		if (lootTables != null)
			return lootTables;
		return lootTables = ((HolderLookup.Provider) server.reloadableRegistries().lookup())
			.lookupOrThrow(Registries.LOOT_TABLE)
			.listElementIds()
			.filter(key -> key.location().getPath().startsWith("chests/"))
			.toList();
	}

	@Override
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(() -> {
			List<ServerPlayer> players = playerSupplier.get();
			LimitConfig config = getPlugin().getLimitConfig();
			int playerLimit = config.getEntityLimit(getEffectName().replace("entity_", ""));
			if (playerLimit > 0) {
				boolean hostsBypass = config.hostsBypass();
				AtomicInteger victims = new AtomicInteger();
				players = players.stream()
					.sorted(Comparator.comparing(plugin::globalEffectsUsableFor))
					.takeWhile(player -> victims.getAndAdd(1) < playerLimit || (hostsBypass && plugin.globalEffectsUsableFor(player)))
					.toList();
			}

			CCEffectResponse tryExecute = tryExecute(players, request, ccPlayer);
			if (tryExecute != null) return tryExecute;

			boolean success = false;
			Component name = plugin.getViewerComponentOrNull(request, false);

			for (ServerPlayer player : players) {
				try {
					success |= spawnEntity(name, player) != null;
				} catch (Exception e) {
					plugin.getSLF4JLogger().error("Failed to spawn entity", e);
				}
			}

			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_PERMANENT, "Failed to spawn entity"); // TODO: hide?
		}, plugin.getSyncExecutor()));
	}

	@Blocking
	protected E spawnEntity(@Nullable Component viewer, @NotNull ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		E entity = getRandomEntityType().create(player.serverLevel(), EntitySpawnReason.COMMAND);
		if (entity == null)
			throw new IllegalStateException("Could not spawn entity");
		// set variables
		entity.setPos(player.position());
		if (viewer != null) {
			entity.setCustomName(plugin.toNative(viewer, player));
			entity.setCustomNameVisible(true);
		}
		if (entity instanceof Mob mob)
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.COMMAND, null);
		if (entity instanceof TamableAnimal tamable)
			tamable.tame(player);
		if (entity instanceof LivingEntity livingEntity)
			livingEntity.cc$setViewerSpawned();
		if (entity instanceof Wolf wolf) {
			wolf.setCollarColor(randomElementFrom(DyeColor.values())); // TODO: crash
			wolf.setVariant(randomElementFrom(level.registryAccess().lookupOrThrow(Registries.WOLF_VARIANT).listElements()));
		}
		if (entity instanceof MushroomCow mooshroom && RandomUtil.RNG.nextDouble() < MUSHROOM_COW_BROWN_CHANCE)
			mooshroom.setVariant(MushroomCow.Variant.BROWN);
		if (entity instanceof AbstractHorse horse) {
			if (horse.canUseSlot(EquipmentSlot.BODY) && RandomUtil.RNG.nextBoolean()) {
				List<Item> items = horseArmor.computeIfAbsent(entityType, $ -> BuiltInRegistries.ITEM.stream()
					.filter(item -> {
						Equippable equippable = item.components().get(DataComponents.EQUIPPABLE);
						if (equippable == null) return false;
						return equippable.slot().getType() == EquipmentSlot.Type.ANIMAL_ARMOR;
					})
					.toList());
				horse.getSlot(401).set(new ItemStack(randomElementFrom(items)));
			}
			horse.setOwnerUUID(player.getUUID());
			horse.setTamed(true);
		}
		if (entity instanceof Sheep sheep) // TODO: jeb
			sheep.setColor(randomElementFrom(DyeColor.values()));
		if (entity instanceof Saddleable saddleable && RandomUtil.RNG.nextBoolean())
			saddleable.equipSaddle(new ItemStack(Items.SADDLE), null);
		if (entity instanceof EnderMan enderman)
			enderman.setCarriedBlock(randomElementFrom(BuiltInRegistries.BLOCK).defaultBlockState());
		if (entity instanceof AbstractChestedHorse chested)
			chested.setChest(RandomUtil.RNG.nextBoolean());
		if (entity instanceof Frog frog)
			frog.setVariant(randomElementFrom(BuiltInRegistries.FROG_VARIANT.listElements()));
		if (entity instanceof Axolotl axolotl)
			axolotl.setVariant(randomElementFrom(Axolotl.Variant.values()));
		if (entity instanceof Rabbit rabbit)
			rabbit.setVariant(weightedRandom(RABBIT_VARIANTS));
		if (entity instanceof VillagerDataHolder villager)
			villager.setVariant(randomElementFrom(BuiltInRegistries.VILLAGER_TYPE));
		if (entity instanceof ContainerEntity container)
			container.setContainerLootTable(randomElementFrom(getLootTables(level.getServer())));

		// add random armor to armor stands
		if (entity instanceof ArmorStand armorStand) {
			// could add some chaos (GH#64) here eventually
			// chaos idea: set drop chance for each slot to a random float
			List<EquipmentSlot> slots = new ArrayList<>(humanoidArmor.keySet());
			Collections.shuffle(slots, random);
			// begins as a 1 in 4 chance to add a random item but becomes less likely each time
			// an item is added
			int odds = ENTITY_ARMOR_START;
			for (EquipmentSlot type : slots) {
				if (random.nextInt(odds) > 0)
					continue;
				odds += ENTITY_ARMOR_INC;
				ItemStack item = new ItemStack(randomElementFrom(humanoidArmor.get(type)));
				plugin.commandRegister()
						.getCommandByName("lootbox", LootboxCommand.class)
						.randomlyModifyItem(item, odds / ENTITY_ARMOR_START, level.registryAccess());
				armorStand.setItemSlot(type, item);
			}

			if (RNG.nextBoolean()) {
				armorStand.setShowArms(true);
				for (EquipmentSlot slot : HANDS) {
					if (!RNG.nextBoolean()) continue;
					armorStand.setItemSlot(slot, plugin.commandRegister().getCommandByName("lootbox", LootboxCommand.class).createRandomItem(RNG.nextInt(6), level.registryAccess()));
				}
			}
		}

		// spawn entity
		level.addFreshEntity(entity);
		return entity;
	}
}
