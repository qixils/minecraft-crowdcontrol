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
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_ARMOR_INC;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_ARMOR_START;
import static dev.qixils.crowdcontrol.common.util.RandomUtil.randomElementFrom;

@Getter
public class SummonEntityCommand<E extends Entity> extends ImmediateCommand implements EntityCommand<E> {
	private final Map<EquipmentSlot, List<Item>> armor;
	protected final EntityType<E> entityType;
	private final String effectName;
	private final Component displayName;
	private @Nullable List<ResourceLocation> lootTables = null;
	private final Map<EntityType<?>, List<Item>> horseArmor = new HashMap<>();

	public SummonEntityCommand(FabricCrowdControlPlugin plugin, EntityType<E> entityType) {
		super(plugin);
		this.entityType = entityType;
		this.effectName = "entity_" + csIdOf(Registry.ENTITY_TYPE.getKey(entityType));
		this.displayName = Component.translatable("cc.effect.summon_entity.name", entityType.getDescription());

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
	private List<ResourceLocation> getLootTables(MinecraftServer server) {
		if (lootTables != null)
			return lootTables;
		return lootTables = server.getLootTables().getIds().stream().filter(location -> location.getPath().startsWith("chests/")).toList();
	}

	@NotNull
	@Override
	public Response.Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		Response.Builder tryExecute = tryExecute(players, request);
		if (tryExecute != null) return tryExecute;

		LimitConfig config = getPlugin().getLimitConfig();
		int maxVictims = config.getItemLimit(Registry.ENTITY_TYPE.getKey(entityType).getPath());

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
		ServerLevel level = player.getLevel();
		E entity = entityType.create(player.level);
		if (entity == null)
			throw new IllegalStateException("Could not spawn entity");
		// set variables
		entity.setPos(player.position());
		if (viewer != null) {
			entity.setCustomName(plugin.toNative(viewer, player));
			entity.setCustomNameVisible(true);
		}
		if (entity instanceof Mob mob)
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
		if (entity instanceof TamableAnimal tamable)
			tamable.tame(player);
		if (entity instanceof LivingEntity)
			Components.VIEWER_MOB.get(entity).setViewerSpawned();
		if (entity instanceof Boat boat)
			boat.setType(randomElementFrom(Boat.Type.class));
		if (entity instanceof Wolf wolf)
			wolf.setCollarColor(randomElementFrom(DyeColor.class));
		if (entity instanceof AbstractHorse horse) {
			if (horse.canWearArmor() && RandomUtil.RNG.nextBoolean()) {
				List<Item> items = horseArmor.computeIfAbsent(entityType, $ -> Registry.ITEM.stream().filter(item -> horse.isArmor(new ItemStack(item))).toList());
				horse.getSlot(401).set(new ItemStack(randomElementFrom(items)));
			}
			horse.setOwnerUUID(player.getUUID());
			horse.setTamed(true);
		}
		if (entity instanceof Sheep sheep) // TODO: jeb
			sheep.setColor(randomElementFrom(DyeColor.class));
		if (entity instanceof Saddleable saddleable && RandomUtil.RNG.nextBoolean())
			saddleable.equipSaddle(null);
		if (entity instanceof EnderMan enderman)
			enderman.setCarriedBlock(randomElementFrom(Registry.BLOCK).defaultBlockState());
		if (entity instanceof AbstractChestedHorse chested)
			chested.setChest(RandomUtil.RNG.nextBoolean());
		if (entity instanceof Frog frog)
			frog.setVariant(randomElementFrom(Registry.FROG_VARIANT));
		if (entity instanceof ContainerEntity container)
			container.setLootTable(randomElementFrom(getLootTables(level.getServer())));

		// add random armor to armor stands
		if (entity instanceof ArmorStand) {
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
				entity.setItemSlot(type, item);
			}
		}

		// spawn entity
		level.addFreshEntity(entity);
		return entity;
	}
}
