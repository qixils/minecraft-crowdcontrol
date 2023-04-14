package dev.qixils.crowdcontrol.plugin.sponge7.commands;

import dev.qixils.crowdcontrol.common.LimitConfig;
import dev.qixils.crowdcontrol.plugin.sponge7.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge7.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.sponge7.utils.SpongeTextUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.RabbitType;
import org.spongepowered.api.data.type.RabbitTypes;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.HeldEquipmentType;
import org.spongepowered.api.item.inventory.property.EquipmentSlotType;

import java.util.*;

import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_ARMOR_INC;
import static dev.qixils.crowdcontrol.common.command.CommandConstants.ENTITY_ARMOR_START;
import static dev.qixils.crowdcontrol.common.util.RandomUtil.*;

@Getter
public class SummonEntityCommand extends ImmediateCommand implements EntityCommand {
	private final Map<EquipmentType, List<ItemType>> armor;
	protected final EntityType entityType;
	private final String effectName;
	private final Component displayName;
	private static final Map<RabbitType, Integer> RABBIT_VARIANTS;

	static {
		Map<RabbitType, Integer> variants = new HashMap<>();
		variants.put(RabbitTypes.BLACK, 16);
		variants.put(RabbitTypes.BLACK_AND_WHITE, 16);
		variants.put(RabbitTypes.BROWN, 16);
		variants.put(RabbitTypes.GOLD, 16);
		variants.put(RabbitTypes.SALT_AND_PEPPER, 16);
		variants.put(RabbitTypes.WHITE, 16);
		variants.put(RabbitTypes.KILLER, 1);
		RABBIT_VARIANTS = Collections.unmodifiableMap(variants);
	}

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
		Response.Builder tryExecute = tryExecute(players, request);
		if (tryExecute != null) return tryExecute;

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
				spawnEntity(plugin.getViewerComponentOrNull(request, false), player);
				victims++;
			}

			// second pass (guests)
			for (Player player : players) {
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
	protected Entity spawnEntity(@Nullable Component viewer, @NotNull Player player) {
		Entity entity = player.getLocation().createEntity(entityType);
		// set variables
		if (viewer != null) {
			entity.offer(Keys.DISPLAY_NAME, plugin.getSpongeSerializer().serialize(GlobalTranslator.render(viewer, player.getLocale())));
			entity.offer(Keys.CUSTOM_NAME_VISIBLE, true);
		}
		entity.offer(Keys.TAMED_OWNER, Optional.of(player.getUniqueId()));
		entity.offer(Keys.TREE_TYPE, randomElementFrom(plugin.getRegistry().getAllOf(TreeType.class)));
		entity.offer(Keys.DYE_COLOR, randomElementFrom(plugin.getRegistry().getAllOf(DyeColor.class)));
		// brown mooshroom cows are not in 1.12.2
		// TODO horse armor, chest, saddle?
		entity.offer(Keys.PIG_SADDLE, RNG.nextBoolean());
		// enderman held block API is unavailable in API v7
		entity.offer(Keys.RABBIT_TYPE, weightedRandom(RABBIT_VARIANTS));
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
				ItemStack item = ItemStack.of(randomElementFrom(armor.get(type)));
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
