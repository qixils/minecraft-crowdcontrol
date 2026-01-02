package dev.qixils.crowdcontrol.plugin.paper.commands;

import dev.qixils.crowdcontrol.common.custom.CustomCommandAction;
import dev.qixils.crowdcontrol.common.custom.CustomCommandData;
import dev.qixils.crowdcontrol.plugin.paper.PaperCrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.paper.RegionalCommandSync;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.math.FinePosition;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
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

	public boolean onSummonEntity(PaperCrowdControlPlugin plugin, Player player, CustomCommandAction action, PublicEffectPayload request, CCPlayer ccPlayer) {
		String entityStr = action.getString("type", "minecraft:pig");
		NamespacedKey entityId = Objects.requireNonNullElseGet(NamespacedKey.fromString(entityStr), () -> new NamespacedKey("minecraft", "pig"));
		EntityType entityType = Registry.ENTITY_TYPE.getOrThrow(entityId);

		String nbt = action.getString("nbt", null);

		Location location;
		String posStr = action.getString("pos", "~ ~ ~");
		try {
			// TODO: replace nms `createCommandSourceStack` with paper api should one ever exist
			CommandSourceStack sourceStack = ((org.bukkit.craftbukkit.entity.CraftPlayer) player).getHandle().createCommandSourceStack();
			FinePosition pos = ArgumentTypes.finePosition().parse(new com.mojang.brigadier.StringReader(posStr.strip())).resolve(sourceStack);
			location = player.getLocation().set(pos.x(), pos.y(), pos.z());
		} catch (Exception e) {
			location = player.getLocation();
			plugin.getSLF4JLogger().warn("Failed to parse pos tag", e);
		}

		Location finalPos = location;
		if (nbt == null) {
			player.getWorld().spawnEntity(location, entityType, true);
		} else {
			// TODO: replace with EntityFactory once it is able to spawn passengers
			try {
				net.minecraft.server.level.ServerLevel level = ((org.bukkit.craftbukkit.entity.CraftPlayer) player).getHandle().level();
				net.minecraft.nbt.CompoundTag tag = net.minecraft.commands.arguments.CompoundTagArgument.compoundTag().parse(new com.mojang.brigadier.StringReader(nbt));
				tag = tag.copy();
				tag.putString("id", entityType.key().toString());
				net.minecraft.world.entity.Entity entity = net.minecraft.world.entity.EntityType.loadEntityRecursive(tag, level, net.minecraft.world.entity.EntitySpawnReason.COMMAND, entityx -> {
					entityx.spawnReason = CreatureSpawnEvent.SpawnReason.COMMAND; // Paper - Entity#getEntitySpawnReason
					entityx.getBukkitEntity().teleport(finalPos, PlayerTeleportEvent.TeleportCause.PLUGIN);
					return entityx;
				});
				if (entity == null) return false;
				if (entity instanceof net.minecraft.world.entity.Mob mob) mob.finalizeSpawn(level, level.getCurrentDifficultyAt(net.minecraft.core.BlockPos.containing(location.x(), location.y(), location.z())), net.minecraft.world.entity.EntitySpawnReason.COMMAND, null);
				return level.tryAddFreshEntityWithPassengers(entity, CreatureSpawnEvent.SpawnReason.COMMAND);
			} catch (Exception e) {
				plugin.getSLF4JLogger().warn("Failed to spawn entity", e);
				return false;
			}
		}

		return true;
	}

	public boolean onGiveItem(PaperCrowdControlPlugin plugin, Player player, CustomCommandAction action, PublicEffectPayload request, CCPlayer ccPlayer) {
		String itemStr = action.getString("item", "minecraft:dirt");
		ItemStack stack;
		try {
			stack = Bukkit.getItemFactory().createItemStack(itemStr);
		} catch (Exception e) {
			plugin.getSLF4JLogger().warn("Failed to generate item stack", e);
			return false;
		}

		int quantity = Math.max(1, Math.min(100 * stack.getMaxStackSize(), action.getInt("quantity", 1)));
		while (quantity > 0) {
			int spawnAmt = Math.min(stack.getMaxStackSize(), quantity);
			quantity -= spawnAmt;
			ItemStack spawnStack = stack.clone();
			spawnStack.setAmount(spawnAmt);
			GiveItemCommand.giveItemTo(player, spawnStack);
		}

		return true;
	}

	public boolean onRunCommand(PaperCrowdControlPlugin plugin, Player player, CustomCommandAction action, PublicEffectPayload request, CCPlayer ccPlayer) {
		String _cmd = action.getString("command", null);
		if (_cmd == null || _cmd.isEmpty()) return false;

		String commandLine = _cmd.startsWith("/")
			? _cmd.substring(1)
			: _cmd;

		try {
			net.minecraft.commands.Commands commands = ((org.bukkit.craftbukkit.CraftServer) Bukkit.getServer()).getServer().getCommands();
			com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher = commands.getDispatcher();
			com.mojang.brigadier.ParseResults<net.minecraft.commands.CommandSourceStack> results = dispatcher.parse(commandLine, ((org.bukkit.craftbukkit.entity.CraftPlayer) player).getHandle().createCommandSourceStack().withSuppressedOutput());

			// we have Commands.finishParsing at home:
			// (this catches undefined commands, since finishParsing itself is private and performCommand does not throw an exception if it returns null)
			net.minecraft.commands.Commands.validateParseResults(results);
			com.mojang.brigadier.context.ContextChain.tryFlatten(results.getContext().build(commandLine)).orElseThrow(() -> com.mojang.brigadier.exceptions.CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(results.getReader()));

			// ok we can run the command now; if it errors then `return true;` is skipped
			commands.performCommand(results, commandLine, true);
			return true;
		} catch (Exception e) {
			plugin.getSLF4JLogger().warn("Failed to run command", e);
		}
		return false;
	}

	public final @NotNull Map<String, Executor> executors = Map.ofEntries(
		Map.entry("summon-entity", this::onSummonEntity),
		Map.entry("give-item", this::onGiveItem),
		Map.entry("run-command", this::onRunCommand)
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
			if (!executors.containsKey(action.type())) throw new RuntimeException("Invalid action \"" + action.type() + '"');
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
			Executor executor = executors.get(action.type());
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
