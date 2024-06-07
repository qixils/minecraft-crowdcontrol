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
import net.kyori.adventure.text.Component;
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
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.common.util.RandomUtil.*;

@Getter
public class SummonEntityCommand<E extends Entity> extends ImmediateCommand implements EntityCommand<E> {
	private static final Set<EquipmentSlot> HANDS = Arrays.stream(EquipmentSlot.values()).filter(slot -> slot.getType() == EquipmentSlot.Type.HAND).collect(Collectors.toSet());
	private final Map<EquipmentSlot, List<ArmorItem>> humanoidArmor;
	protected final EntityType<E> entityType;
	private final String effectName;
	private final Component displayName;
	private List<ResourceKey<LootTable>> lootTables = null;
	private final Map<EntityType<?>, List<ArmorItem>> horseArmor = new HashMap<>();
	private static final Map<Rabbit.Variant, Integer> RABBIT_VARIANTS = Map.of(
			Rabbit.Variant.BLACK, 16,
			Rabbit.Variant.BROWN, 16,
			Rabbit.Variant.GOLD, 16,
			Rabbit.Variant.SALT, 16,
			Rabbit.Variant.WHITE, 16,
			Rabbit.Variant.WHITE_SPLOTCHED, 16,
			Rabbit.Variant.EVIL, 1
	);

	public SummonEntityCommand(FabricCrowdControlPlugin plugin, EntityType<E> entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "entity_" + csIdOf(BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
		this.displayName = Component.translatable("cc.effect.summon_entity.name", entityType.getDescription());

		// pre-compute the map of valid armor pieces
		Map<EquipmentSlot, List<ArmorItem>> armor = new HashMap<>(4);
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof ArmorItem armorItem) {
				EquipmentSlot slot = armorItem.getEquipmentSlot();
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

	private List<ResourceKey<LootTable>> getLootTables(MinecraftServer server) {
		if (lootTables != null)
			return lootTables;
		return lootTables = server.registryAccess()
			.lookupOrThrow(Registries.LOOT_TABLE)
			.listElements()
			.flatMap(reference -> reference.unwrapKey().stream())
			.filter(key -> key.location().getPath().startsWith("chests/"))
			.toList();
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder tryExecute = tryExecute(players, request);
		if (tryExecute != null) return tryExecute;

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
				spawnEntity(plugin.getViewerComponentOrNull(request, false), player);
				victims++;
			}

			// second pass (guests)
			for (ServerPlayer player : players) {
				if (maxVictims > 0 && victims >= maxVictims)
					break;
				if (isHost(player))
					continue;
				spawnEntity(plugin.getViewerComponentOrNull(request, false), player);
				victims++;
			}
		});
		return request.buildResponse().type(ResultType.SUCCESS);
	}

	@Blocking
	protected E spawnEntity(@Nullable Component viewer, @NotNull ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		E entity = entityType.create(player.serverLevel());
		if (entity == null)
			throw new IllegalStateException("Could not spawn entity");
		// set variables
		entity.setPos(player.position());
		if (viewer != null) {
			entity.setCustomName(plugin.toNative(viewer, player));
			entity.setCustomNameVisible(true);
		}
		if (entity instanceof Mob mob)
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
		if (entity instanceof TamableAnimal tamable)
			tamable.tame(player);
		if (entity instanceof LivingEntity)
			Components.VIEWER_MOB.get(entity).setViewerSpawned();
		if (entity instanceof Boat boat)
			boat.setVariant(randomElementFrom(Boat.Type.class));
		if (entity instanceof Wolf wolf) {
			wolf.setCollarColor(randomElementFrom(DyeColor.class));
			wolf.setVariant(randomElementFrom(level.registryAccess().registryOrThrow(Registries.WOLF_VARIANT).holders()));
		}
		if (entity instanceof MushroomCow mooshroom && RandomUtil.RNG.nextDouble() < MUSHROOM_COW_BROWN_CHANCE)
			mooshroom.setVariant(MushroomCow.MushroomType.BROWN);
		if (entity instanceof AbstractHorse horse) {
			if (horse.canUseSlot(EquipmentSlot.BODY) && RandomUtil.RNG.nextBoolean()) {
				List<ArmorItem> items = horseArmor.computeIfAbsent(entityType, $ -> BuiltInRegistries.ITEM.stream()
					.map(item -> item instanceof ArmorItem armorItem ? armorItem : null)
					.filter(Objects::nonNull)
					.filter(item -> item.getEquipmentSlot().getType() == EquipmentSlot.Type.ANIMAL_ARMOR && horse.isBodyArmorItem(new ItemStack(item)))
					.toList());
				horse.getSlot(401).set(new ItemStack(randomElementFrom(items)));
			}
			horse.setOwnerUUID(player.getUUID());
			horse.setTamed(true);
		}
		if (entity instanceof Sheep sheep) // TODO: jeb
			sheep.setColor(randomElementFrom(DyeColor.class));
		if (entity instanceof Saddleable saddleable && RandomUtil.RNG.nextBoolean())
			saddleable.equipSaddle(new ItemStack(Items.SADDLE), null);
		if (entity instanceof EnderMan enderman)
			enderman.setCarriedBlock(randomElementFrom(BuiltInRegistries.BLOCK).defaultBlockState());
		if (entity instanceof AbstractChestedHorse chested)
			chested.setChest(RandomUtil.RNG.nextBoolean());
		if (entity instanceof Frog frog)
			frog.setVariant(randomElementFrom(BuiltInRegistries.FROG_VARIANT.holders()));
		if (entity instanceof Axolotl axolotl)
			axolotl.setVariant(randomElementFrom(Axolotl.Variant.class));
		if (entity instanceof Rabbit rabbit)
			rabbit.setVariant(weightedRandom(RABBIT_VARIANTS));
		if (entity instanceof VillagerDataHolder villager)
			villager.setVariant(randomElementFrom(BuiltInRegistries.VILLAGER_TYPE));
		if (entity instanceof ContainerEntity container)
			container.setLootTable(randomElementFrom(getLootTables(level.getServer())));

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
