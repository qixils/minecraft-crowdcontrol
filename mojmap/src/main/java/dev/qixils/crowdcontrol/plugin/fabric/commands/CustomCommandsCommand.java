package dev.qixils.crowdcontrol.plugin.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.ContextChain;
import dev.qixils.crowdcontrol.common.custom.CustomCommandAction;
import dev.qixils.crowdcontrol.common.custom.CustomCommandData;
import dev.qixils.crowdcontrol.common.util.ThreadUtil;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCommand;
import dev.qixils.crowdcontrol.plugin.fabric.ModdedCrowdControlPlugin;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Getter
public class CustomCommandsCommand extends ModdedCommand {

	@FunctionalInterface
	public interface Executor {
		boolean execute(ModdedCrowdControlPlugin plugin, ServerPlayer player, CustomCommandAction action, PublicEffectPayload request, CCPlayer ccPlayer);
	}

	public static boolean onSummonEntity(ModdedCrowdControlPlugin plugin, ServerPlayer player, CustomCommandAction action, PublicEffectPayload request, CCPlayer ccPlayer) {
		ServerLevel level = player.level();

		String entityStr = action.getString("type", "minecraft:pig");
		ResourceLocation entityId = Objects.requireNonNullElseGet(ResourceLocation.tryParse(entityStr), () -> ResourceLocation.fromNamespaceAndPath("minecraft", "pig"));

		Registry<EntityType<?>> entities = player.registryAccess().lookupOrThrow(Registries.ENTITY_TYPE);
		Holder.Reference<EntityType<?>> entityType = entities.get(entityId).or(() -> {
			var pig = entities.wrapAsHolder(EntityType.PIG);
			if (pig instanceof Holder.Reference<EntityType<?>> refPig) return Optional.of(refPig);
			return Optional.empty();
		}).orElse(null);
		if (entityType == null) return false;

		String nbt = action.getString("nbt", null);
		CompoundTag tag = new CompoundTag();
		try {
			if (nbt != null) tag = CompoundTagArgument.compoundTag().parse(new StringReader(nbt));
		} catch (Exception e) {
			plugin.getSLF4JLogger().warn("Failed to parse NBT tag", e);
		}
		tag = tag.copy();
		tag.putString("id", entityType.key().location().toString());

		Vec3 pos;
		try {
			String posStr = action.getString("pos", "~ ~ ~");
			pos = Vec3Argument.vec3().parse(new StringReader(posStr)).getPosition(player.createCommandSourceStack());
		} catch (Exception e) {
			pos = player.position();
			plugin.getSLF4JLogger().warn("Failed to parse pos tag", e);
		}

		Vec3 finalPos = pos;
		Entity entity = EntityType.loadEntityRecursive(tag, level, EntitySpawnReason.COMMAND, entityx -> {
			entityx.snapTo(finalPos.x, finalPos.y, finalPos.z, entityx.getYRot(), entityx.getXRot());
			return entityx;
		});
		if (entity == null) return false;

		if (nbt == null && entity instanceof Mob mob) mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), EntitySpawnReason.COMMAND, null);

		return level.tryAddFreshEntityWithPassengers(entity);
	}

	public static boolean onGiveItem(ModdedCrowdControlPlugin plugin, ServerPlayer player, CustomCommandAction action, PublicEffectPayload request, CCPlayer ccPlayer) {
		String itemStr = action.getString("item", "minecraft:dirt");
		ItemStack stack;
		try {
			ItemParser.ItemResult result = new ItemParser(player.registryAccess()).parse(new StringReader(itemStr));
			ItemInput input = new ItemInput(result.item(), result.components());
			stack = input.createItemStack(1, false);
		} catch (Exception e) {
			plugin.getSLF4JLogger().warn("Failed to generate item stack", e);
			return false;
		}

		int quantity = Math.max(1, Math.min(100 * stack.getMaxStackSize(), action.getInt("quantity", 1)));
		while (quantity > 0) {
			int spawnAmt = Math.min(stack.getMaxStackSize(), quantity);
			quantity -= spawnAmt;
			ItemStack spawnStack = stack.copyWithCount(spawnAmt);
			GiveItemCommand.giveItemTo(player, spawnStack);
		}

		return true;
	}

	public static boolean onRunCommand(ModdedCrowdControlPlugin plugin, ServerPlayer player, CustomCommandAction action, PublicEffectPayload request, CCPlayer ccPlayer) {
		String _cmd = action.getString("command", null);
		if (_cmd == null || _cmd.isEmpty()) return false;

		String commandLine = _cmd.startsWith("/")
			? _cmd.substring(1)
			: _cmd;

		try {
			Commands commands = plugin.server().getCommands();
			CommandDispatcher<CommandSourceStack> dispatcher = commands.getDispatcher();
			ParseResults<CommandSourceStack> results = dispatcher.parse(commandLine, player.createCommandSourceStack().withSuppressedOutput());

			CommandSourceStack commandSourceStack = results.getContext().getSource();
			ContextChain<CommandSourceStack> contextChain = Commands.finishParsing(results, commandLine, commandSourceStack);

			if (contextChain == null) throw new RuntimeException("Unknown command `" + _cmd + '`');

			Commands.executeCommandInContext(
				commandSourceStack,
				executionContext -> ExecutionContext.queueInitialCommandExecution(executionContext, commandLine, contextChain, commandSourceStack, CommandResultCallback.EMPTY)
			);
			return true;
		} catch (Exception e) {
			plugin.getSLF4JLogger().warn("Failed to run command", e);
		}
		return false;
	}

	public static final @NotNull Map<String, Executor> EXECUTORS = Map.ofEntries(
		Map.entry("summon-entity", CustomCommandsCommand::onSummonEntity),
		Map.entry("give-item", CustomCommandsCommand::onGiveItem),
		Map.entry("run-command", CustomCommandsCommand::onRunCommand)
	);


	private final String effectName;
	private final CustomCommandData data;
	private final byte priority = 100;
	private final int price;
	private final boolean inactive = false;

	public CustomCommandsCommand(ModdedCrowdControlPlugin plugin, CustomCommandData data) {
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
	public void execute(@NotNull Supplier<@NotNull List<@NotNull ServerPlayer>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		ccPlayer.sendResponse(ThreadUtil.waitForSuccess(request, () -> {
			boolean success = false;
			for (ServerPlayer player : playerSupplier.get()) {
				for (CustomCommandAction action : data.actions()) {
					Executor executor = EXECUTORS.get(action.type());
					if (executor == null) continue;

					try {
						success |= executor.execute(getPlugin(), player, action, request, ccPlayer);
					} catch (Exception e) {
						plugin.getSLF4JLogger().warn("Failed to execute custom action", e);
					}
				}
			}

			return success
				? new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.SUCCESS)
				: new CCInstantEffectResponse(request.getRequestId(), ResponseStatus.FAIL_PERMANENT, "Failed to execute custom actions");
		}));
	}
}
