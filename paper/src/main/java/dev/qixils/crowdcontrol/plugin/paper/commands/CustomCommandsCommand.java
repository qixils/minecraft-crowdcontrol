package dev.qixils.crowdcontrol.plugin.paper.commands;

import com.mojang.brigadier.StringReader;
import dev.qixils.crowdcontrol.common.custom.CustomCommandAction;
import dev.qixils.crowdcontrol.common.custom.CustomCommandData;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

@Getter
public class CustomCommandsCommand extends RegionalCommandSync {

	@FunctionalInterface
	public interface Executor {
		boolean execute(PaperCrowdControlPlugin plugin, Player player, CustomCommandAction action, PublicEffectPayload request, CCPlayer ccPlayer);
	}

	public static boolean onSummonEntity(PaperCrowdControlPlugin plugin, Player player, CustomCommandAction action, PublicEffectPayload request, CCPlayer ccPlayer) {
		CraftPlayer craftPlayer = (CraftPlayer) player;
		ServerLevel level = (ServerLevel) craftPlayer.getHandle().level(); // forwards compat with 1.21.6+ (removes serverLevel())

		String entityStr = action.getOption("type", String.class, "minecraft:pig");
		NamespacedKey entityId = Objects.requireNonNullElseGet(NamespacedKey.fromString(entityStr), () -> new NamespacedKey("minecraft", "pig"));
		EntityType entityType = Registry.ENTITY_TYPE.getOrThrow(entityId);

		String nbt = action.getOption("nbt", String.class, null);
		CompoundTag tag = new CompoundTag();
		try {
			if (nbt != null) tag = CompoundTagArgument.compoundTag().parse(new StringReader(nbt.strip()));
		} catch (Exception e) {
			plugin.getSLF4JLogger().warn("Failed to parse NBT tag", e);
		}
		tag = tag.copy();
		tag.putString("id", entityType.key().toString());

		Location location;
		String posStr = action.getOption("pos", String.class, "~ ~ ~");
		try {
			Vec3 pos = Vec3Argument.vec3().parse(new StringReader(posStr.strip())).getPosition(craftPlayer.getHandle().createCommandSourceStack());
			location = player.getLocation().set(pos.x, pos.y, pos.z);
		} catch (Exception e) {
			location = player.getLocation();
			plugin.getSLF4JLogger().warn("Failed to parse pos tag", e);
		}

		Location finalPos = location;
		net.minecraft.world.entity.Entity entity = net.minecraft.world.entity.EntityType.loadEntityRecursive(tag, level, EntitySpawnReason.COMMAND, entityx -> {
			entityx.spawnReason = CreatureSpawnEvent.SpawnReason.COMMAND; // Paper - Entity#getEntitySpawnReason
			entityx.getBukkitEntity().teleport(finalPos, PlayerTeleportEvent.TeleportCause.PLUGIN);
			return entityx;
		});
		if (entity == null) return false;

		if (nbt == null && entity instanceof net.minecraft.world.entity.Mob mob) mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.COMMAND, null);

		return level.tryAddFreshEntityWithPassengers(entity);
	}

	public static boolean onGiveItem(PaperCrowdControlPlugin plugin, Player player, CustomCommandAction action, PublicEffectPayload request, CCPlayer ccPlayer) {
		CraftPlayer craftPlayer = (CraftPlayer) player;
		String itemStr = action.getOption("item", String.class, "minecraft:dirt");
		net.minecraft.world.item.ItemStack stack;
		try {
			ItemParser.ItemResult result = new ItemParser(craftPlayer.getHandle().registryAccess()).parse(new StringReader(itemStr));
			ItemInput input = new ItemInput(result.item(), result.components());
			stack = input.createItemStack(1, false);
		} catch (Exception e) {
			plugin.getSLF4JLogger().warn("Failed to generate item stack", e);
			return false;
		}
		ItemStack bukkit = CraftItemStack.asBukkitCopy(stack);

		int quantity = Math.max(1, Math.min(100 * stack.getMaxStackSize(), action.getInt("quantity", 1)));
		while (quantity > 0) {
			int spawnAmt = Math.min(stack.getMaxStackSize(), quantity);
			quantity -= spawnAmt;
			ItemStack spawnStack = bukkit.clone();
			spawnStack.setAmount(spawnAmt);
			GiveItemCommand.giveItemTo(player, spawnStack);
		}

		return true;
	}

	public static final @NotNull Map<String, Executor> EXECUTORS = Map.ofEntries(
		Map.entry("summon-entity", CustomCommandsCommand::onSummonEntity),
		Map.entry("give-item", CustomCommandsCommand::onGiveItem)
	);


	private final String effectName;
	private final CustomCommandData data;
	private final byte priority = 100;
	private final int price;
	private final boolean inactive = false;

	public CustomCommandsCommand(PaperCrowdControlPlugin plugin, CustomCommandData data) {
		super(plugin);
		this.data = data;

		if (data.name() == null) throw new RuntimeException("Effect name undefined");
		String plain = plugin.getTextUtil().asPlain(data.name());
		effectName = ("custom_" + plain + '_' + Integer.toString(plain.hashCode(), Character.MAX_RADIX)).replaceAll("[^a-zA-Z0-9_]", "_");

		this.price = Math.max(1, Math.min(100000, data.price()));

		if (data.actions().isEmpty()) {
			throw new RuntimeException("No command actions defined");
		}

		for (CustomCommandAction action : data.actions()) {
			if (!EXECUTORS.containsKey(action.type())) throw new RuntimeException("Invalid action \"" + action.type() + '"');
		}
	}

	@Override
	public @NotNull Component getDisplayName() {
		return data.name();
	}

	@Override
	public @Nullable String getDescription() {
		return data.description();
	}

	@Override
	public @Nullable String getImage() {
		return data.image();
	}

	@Override
	protected @NotNull CCEffectResponse buildFailure(PublicEffectPayload request, CCPlayer ccPlayer) {
		return new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_PERMANENT, "Failed to execute custom actions");
	}

	@Override
	protected boolean executeRegionallySync(Player player, PublicEffectPayload request, CCPlayer ccPlayer) {
		boolean success = false;
		for (CustomCommandAction action : data.actions()) {
			Executor executor = EXECUTORS.get(action.type());
			if (executor == null) continue;

			try {
				success |= executor.execute(getPlugin(), player, action, request, ccPlayer);
			} catch (Exception e) {
				plugin.getSLF4JLogger().warn("Failed to execute custom action", e);
			}
		}
		return success;
	}
}
