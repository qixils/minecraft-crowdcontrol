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
import net.minecraft.entity.*;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerDataContainer;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.*;
import static dev.qixils.crowdcontrol.common.util.RandomUtil.randomElementFrom;
import static dev.qixils.crowdcontrol.common.util.RandomUtil.weightedRandom;

@Getter
public class SummonEntityCommand<E extends Entity> extends ImmediateCommand implements EntityCommand<E> {
	private final Map<EquipmentSlot, List<Item>> armor;
	protected final EntityType<E> entityType;
	private final String effectName;
	private final Component displayName;
	private @Nullable List<Identifier> lootTables = null;
	private final Map<EntityType<?>, List<Item>> horseArmor = new HashMap<>();
	private static final Map<RabbitEntity.RabbitType, Integer> RABBIT_VARIANTS = Map.of(
			RabbitEntity.RabbitType.BLACK, 16,
			RabbitEntity.RabbitType.BROWN, 16,
			RabbitEntity.RabbitType.GOLD, 16,
			RabbitEntity.RabbitType.SALT, 16,
			RabbitEntity.RabbitType.WHITE, 16,
			RabbitEntity.RabbitType.WHITE_SPLOTCHED, 16,
			RabbitEntity.RabbitType.EVIL, 1
	);

	public SummonEntityCommand(FabricCrowdControlPlugin plugin, EntityType<E> entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "entity_" + csIdOf(Registries.ENTITY_TYPE.getId(entityType));
		this.displayName = Component.translatable("cc.effect.summon_entity.name", entityType.getName());

		// pre-compute the map of valid armor pieces
		Map<EquipmentSlot, List<Item>> armor = new HashMap<>(4);
		for (Item item : Registries.ITEM) {
			if (item instanceof ArmorItem armorItem) {
				EquipmentSlot slot = armorItem.getSlotType();
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
	private List<Identifier> getLootTables(MinecraftServer server) {
		if (lootTables != null)
			return lootTables;
		return lootTables = server.getLootManager().getTableIds().stream().filter(location -> location.getPath().startsWith("chests/")).toList();
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayerEntity> players, @NotNull Request request) {
		Response.Builder tryExecute = tryExecute(players, request);
		if (tryExecute != null) return tryExecute;

		LimitConfig config = getPlugin().getLimitConfig();
		int maxVictims = config.getItemLimit(Registries.ENTITY_TYPE.getId(entityType).getPath());

		sync(() -> {
			int victims = 0;

			// first pass (hosts)
			for (ServerPlayerEntity player : players) {
				if (!config.hostsBypass() && maxVictims > 0 && victims >= maxVictims)
					break;
				if (!isHost(player))
					continue;
				spawnEntity(plugin.getViewerComponentOrNull(request, false), player);
				victims++;
			}

			// second pass (guests)
			for (ServerPlayerEntity player : players) {
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
	protected E spawnEntity(@Nullable Component viewer, @NotNull ServerPlayerEntity player) {
		ServerWorld level = player.getWorld();
		E entity = entityType.create(player.world);
		if (entity == null)
			throw new IllegalStateException("Could not spawn entity");
		// set variables
		entity.setPosition(player.getPos());
		if (viewer != null) {
			entity.setCustomName(plugin.toNative(viewer, player));
			entity.setCustomNameVisible(true);
		}
		if (entity instanceof MobEntity mob)
			mob.initialize(level, level.getLocalDifficulty(entity.getBlockPos()), SpawnReason.MOB_SUMMONED, null, null);
		if (entity instanceof TameableEntity tamable)
			tamable.setOwner(player);
		if (entity instanceof LivingEntity)
			Components.VIEWER_MOB.get(entity).setViewerSpawned();
		if (entity instanceof BoatEntity boat)
			boat.setVariant(randomElementFrom(BoatEntity.Type.class));
		if (entity instanceof WolfEntity wolf)
			wolf.setCollarColor(randomElementFrom(DyeColor.class));
		if (entity instanceof MooshroomEntity mooshroom && RandomUtil.RNG.nextDouble() < MUSHROOM_COW_BROWN_CHANCE)
			mooshroom.setVariant(MooshroomEntity.Type.BROWN);
		if (entity instanceof AbstractHorseEntity horse) {
			if (horse.hasArmorSlot() && RandomUtil.RNG.nextBoolean()) {
				List<Item> items = horseArmor.computeIfAbsent(entityType, $ -> Registries.ITEM.stream().filter(item -> horse.isHorseArmor(new ItemStack(item))).toList());
				horse.getStackReference(401).set(new ItemStack(randomElementFrom(items)));
			}
			horse.setOwnerUuid(player.getUuid());
			horse.setTame(true);
		}
		if (entity instanceof SheepEntity sheep) // TODO: jeb
			sheep.setColor(randomElementFrom(DyeColor.class));
		if (entity instanceof Saddleable saddleable && RandomUtil.RNG.nextBoolean())
			saddleable.saddle(null);
		if (entity instanceof EndermanEntity enderman)
			enderman.setCarriedBlock(randomElementFrom(Registries.BLOCK).getDefaultState());
		if (entity instanceof AbstractDonkeyEntity chested)
			chested.setHasChest(RandomUtil.RNG.nextBoolean());
		if (entity instanceof FrogEntity frog)
			frog.setVariant(randomElementFrom(Registries.FROG_VARIANT));
		if (entity instanceof AxolotlEntity axolotl)
			axolotl.setVariant(randomElementFrom(AxolotlEntity.Variant.class));
		if (entity instanceof RabbitEntity rabbit)
			rabbit.setVariant(weightedRandom(RABBIT_VARIANTS));
		if (entity instanceof VillagerDataContainer villager)
			villager.setVariant(randomElementFrom(Registries.VILLAGER_TYPE));
		if (entity instanceof VehicleInventory container)
			container.setLootTableId(randomElementFrom(getLootTables(level.getServer())));

		// add random armor to armor stands
		if (entity instanceof ArmorStandEntity) {
			// could add some chaos (GH#64) here eventually
			// chaos idea: set drop chance for each slot to a random float
			List<EquipmentSlot> slots = new ArrayList<>(armor.keySet());
			Collections.shuffle(slots, random);
			// begins as a 1 in 4 chance to add a random item but becomes less likely each time
			// an item is added
			int odds = ENTITY_ARMOR_START;
			for (EquipmentSlot type : slots) {
				if (random.nextInt(odds) > 0)
					continue;
				odds += ENTITY_ARMOR_INC;
				ItemStack item = new ItemStack(randomElementFrom(armor.get(type)));
				plugin.commandRegister()
						.getCommandByName("lootbox", LootboxCommand.class)
						.randomlyModifyItem(item, odds / ENTITY_ARMOR_START);
				entity.equipStack(type, item);
			}
		}

		// spawn entity
		level.spawnEntity(entity);
		return entity;
	}
}
