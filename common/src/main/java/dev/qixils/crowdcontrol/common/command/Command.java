package dev.qixils.crowdcontrol.common.command;

import dev.qixils.crowdcontrol.TrackedEffect;
import dev.qixils.crowdcontrol.TriState;
import dev.qixils.crowdcontrol.common.EventListener;
import dev.qixils.crowdcontrol.common.Global;
import dev.qixils.crowdcontrol.common.Plugin;
import dev.qixils.crowdcontrol.common.packets.util.ExtraFeature;
import dev.qixils.crowdcontrol.common.util.CCResponseException;
import dev.qixils.crowdcontrol.common.util.DynamicForwardingAudience;
import dev.qixils.crowdcontrol.common.util.SemVer;
import live.crowdcontrol.cc4j.CCEffect;
import live.crowdcontrol.cc4j.CCPlayer;
import live.crowdcontrol.cc4j.CrowdControl;
import live.crowdcontrol.cc4j.IUserRecord;
import live.crowdcontrol.cc4j.websocket.data.CCInstantEffectResponse;
import live.crowdcontrol.cc4j.websocket.data.ResponseStatus;
import live.crowdcontrol.cc4j.websocket.payload.PublicEffectPayload;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A command which handles incoming effects requested by Crowd Control server.
 *
 * @param <P> class used to represent online players
 */
public interface Command<P> extends CCEffect {

	/**
	 * Gets the plugin that registered this command.
	 *
	 * @return owning plugin
	 */
	@NotNull
	@CheckReturnValue
	Plugin<P, ?> getPlugin();

	/**
	 * Executes this command. This will apply a certain effect to all the targeted {@code players}.
	 * The resulting status of executing the command (or null) is returned.
	 *
	 * @param playerSupplier supplies players to apply the effect to
	 * @param request request that prompted the execution of this command
	 * @param ccPlayer connection that invoked the request
	 */
	void execute(@NotNull Supplier<@NotNull List<@NotNull P>> playerSupplier, @NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer);

	/**
	 * Determines if this command object listens for events dispatched by the Minecraft server API.
	 *
	 * @return if events are being listened to
	 */
	default boolean isEventListener() {
		return getClass().isAnnotationPresent(EventListener.class);
	}

	/**
	 * Gets the internal code name for an effect.
	 * It should match the name of an effect from the project's .cs file.
	 *
	 * @return internal code name
	 */
	@NotNull
	@CheckReturnValue
	String getEffectName();

	/**
	 * The minimum version of the mod that clients must have to use this command.
	 * A value of {@link SemVer#ZERO} indicates that no minimum version is required.
	 *
	 * @return minimum mod version
	 */
	@NotNull
	@CheckReturnValue
	default SemVer getMinimumModVersion() {
		return SemVer.ZERO;
	}

	/**
	 * Gets the {@link ExtraFeature}s required for clients to use this effect.
	 *
	 * @return required extra features
	 */
	default @NotNull Set<ExtraFeature> requiredExtraFeatures() {
		return EnumSet.noneOf(ExtraFeature.class);
	}

	/**
	 * Gets the default display name for this command.
	 *
	 * @return default display name
	 */
	@NotNull
	@CheckReturnValue
	default TranslatableComponent getDefaultDisplayName() {
		return Component.translatable("cc.effect." + getEffectName() + ".name");
	}

	/**
	 * Gets the effect's raw display name. This is used when sending a chat message to streamers
	 * informing them of the activation of an effect.
	 *
	 * <p>Further processing may take place in the {@link #getProcessedDisplayName(PublicEffectPayload)} method.</p>
	 *
	 * @return display name
	 */
	@NotNull
	@CheckReturnValue
	default Component getDisplayName() {
		return getDefaultDisplayName();
	}

	/**
	 * Gets the effect's processed display name. This contains the contents of
	 * {@link #getDisplayName()} and may optionally include additional information such as how long
	 * the command's effects will last.
	 *
	 * @return processed display name
	 */
	@NotNull
	@CheckReturnValue
	default Component getProcessedDisplayName(@NotNull PublicEffectPayload request) {
		Component displayName = getDisplayName();

		if (request.getQuantity() > 0) {
			return getQuantityName(displayName, request);
		}

		if (request.getEffect().getDuration() > 0) {
			return getDurationName(displayName, request);
		}

		return displayName;
	}

	@ApiStatus.Internal
	@ApiStatus.OverrideOnly
	default Component getQuantityName(@NotNull Component displayName, @NotNull PublicEffectPayload request) {
		if (!(displayName instanceof TranslatableComponent))
			return displayName;

		QuantityStyle style = getQuantityStyle();
		if (style == QuantityStyle.NONE)
			return displayName;

		TranslatableComponent translatable = (TranslatableComponent) displayName;
		List<Component> args = new ArrayList<>(translatable.args());
		Component quantity = Component.text(request.getQuantity());
		if (style == QuantityStyle.APPEND || style == QuantityStyle.APPEND_X) {
			args.add(quantity);
		} else if (style == QuantityStyle.PREPEND || style == QuantityStyle.PREPEND_X) {
			args.add(0, quantity);
		}
		translatable = translatable.args(args);

		if ((style == QuantityStyle.APPEND_X || style == QuantityStyle.PREPEND_X) && request.getQuantity() > 1) {
			String[] keyParts = translatable.key().split("\\.");
			if (keyParts.length == 4 && keyParts[0].equals("cc") && keyParts[1].equals("effect") && keyParts[3].equals("name")) {
				keyParts[2] += "_x";
				String key = String.join(".", keyParts);
				translatable = translatable.key(key);
			}
		}

		return translatable;
	}

	@ApiStatus.Internal
	@ApiStatus.OverrideOnly
	default Component getDurationName(@NotNull Component displayName, @NotNull PublicEffectPayload request) {
		Duration duration = Duration.ofSeconds(request.getEffect().getDuration());
		return displayName.append(Component.text(" (" + duration.getSeconds() + "s)", Plugin.DIM_CMD_COLOR));
	}

	/**
	 * Returns which style to use for rendering the quantity of this command's effects.
	 *
	 * @return quantity style
	 */
	@NotNull
	@CheckReturnValue
	default QuantityStyle getQuantityStyle() {
		return QuantityStyle.NONE;
	}

	/**
	 * Gets the executor to use for this command.
	 *
	 * @return command executor
	 */
	@NotNull
	@CheckReturnValue
	@Deprecated
	default Executor getExecutor() {
		/*
		ExecuteUsing.Type executeUsing = Optional.ofNullable(getClass().getAnnotation(ExecuteUsing.class))
			.map(ExecuteUsing::value)
			.orElse(ExecuteUsing.Type.ASYNC);
		Executor executor;
		switch (executeUsing) {
			case SYNC_GLOBAL:
				executor = getPlugin().getSyncExecutor(); // TODO: getGlobalExecutor
				break;
			case ASYNC:
			default:
				executor = Runnable::run;
				break;
		}
		return executor;
		 */
		return Runnable::run;
	}

	@Override
	default void onTrigger(@NotNull PublicEffectPayload request, @NotNull CCPlayer ccPlayer) {
		Plugin<P, ?> plugin = getPlugin();

		plugin.getSLF4JLogger().debug("Executing {} from {}", getDisplayName(), request);
		Function<Boolean, List<P>> playerSupplier = (_force) -> {
			boolean force = _force;

			if (isExclusive() && isArrayActive(ccPlayer) && !force) {
				throw new CCResponseException(new CCInstantEffectResponse(
					request.getRequestId(),
					ResponseStatus.FAIL_TEMPORARY,
					"Conflicting effects were already active"
				));
			}

			if (getPlugin().isPaused() && !force) {
				throw new CCResponseException(new CCInstantEffectResponse(
					request.getRequestId(),
					ResponseStatus.FAIL_TEMPORARY,
					"Cannot run effects whilst game is paused"
				));
			}

			Stream<P> playerStream = plugin.getPlayerManager().getPlayers(request);

			// remove players on older version of the mod
			SemVer minVersion = getMinimumModVersion();
			if (minVersion.isGreaterThan(SemVer.ZERO))
				playerStream = playerStream.filter(player -> plugin.getModVersion(player).orElse(SemVer.ZERO).isAtLeast(minVersion));

			// remove players missing extra features
			Set<ExtraFeature> extraFeatures = requiredExtraFeatures();
			if (!extraFeatures.isEmpty())
				playerStream = playerStream.filter(player -> extraFeatures.stream().allMatch(feature -> plugin.isFeatureAvailable(feature, player)));

			List<P> playerList = playerStream.collect(Collectors.toList());

			if (isGlobal() && playerList.stream().noneMatch(plugin::globalEffectsUsableFor) && !force) {
				throw new CCResponseException(new CCInstantEffectResponse(
					request.getRequestId(),
					ResponseStatus.FAIL_PERMANENT,
					"Global effects cannot be used on the targeted streamer"
				));
			}

			// ensure targets are online / available
			if (playerList.isEmpty() && !force) {
				throw new CCResponseException(new CCInstantEffectResponse(
					request.getRequestId(),
					ResponseStatus.FAIL_TEMPORARY,
					"No available players online"
				));
			}

			// shuffle players so that the recipients of limited effects are random
			Collections.shuffle(playerList);

			return playerList;
		};

		plugin.trackEffect(
			request.getRequestId(),
			new TrackedEffect(
				new DynamicForwardingAudience(() -> plugin.playerMapper().asAudience(playerSupplier.apply(true))),
				request,
				ccPlayer
			)
		);

		getExecutor().execute(() -> {
			try {
				execute(() -> playerSupplier.apply(false), request, ccPlayer);
			} catch (CCResponseException e) {
				ccPlayer.sendResponse(e.getResponse());
			}
		});
	}

	/**
	 * Helper method which executes some code synchronously (i.e. on the server's main thread).
	 *
	 * @param runnable command to execute synchronously
	 */
	default void sync(@NotNull Runnable runnable) {
		getPlugin().getSyncExecutor().execute(runnable);
	}


	/**
	 * Helper method which executes some code asynchronously (i.e. off the server's main thread).
	 *
	 * @param runnable command to execute asynchronously
	 */
	default void async(@NotNull Runnable runnable) {
		getPlugin().getAsyncExecutor().execute(runnable);
	}

	/**
	 * Whether this command can only be run when global effects are enabled
	 * or the targeted players includes a server host.
	 *
	 * @return whether this effect is global
	 */
	default boolean isGlobal() {
		return getClass().isAnnotationPresent(Global.class);
	}

	/**
	 * Whether this command should currently be selectable in the overlay.
	 *
	 * @return whether this effect is selectable
	 */
	default TriState isSelectable(@NotNull IUserRecord user, @NotNull List<P> potentialPlayers) {
		return TriState.UNKNOWN;
	}

	/**
	 * Whether this command should currently be visible in the overlay.
	 *
	 * @return whether this effect is visible
	 */
	default TriState isVisible(@NotNull IUserRecord user, @NotNull List<P> potentialPlayers) {
		return TriState.UNKNOWN;
	}

	default boolean isExclusive() {
		return true;
	}

	/**
	 * Determines if any effects with the provided IDs are active.
	 *
	 * @param player player to check
	 * @param effectIDs effect IDs
	 * @return if effect is active
	 */
	@ApiStatus.NonExtendable
	default boolean isActive(@NotNull CCPlayer player, @NotNull String... effectIDs) {
		CrowdControl cc = getPlugin().getCrowdControl();
		if (cc == null) return false;
		UUID uuid = player.getUuid();
		return Arrays.stream(effectIDs)
			.flatMap(effectID -> Stream.concat(Stream.of(effectID), getPlugin().commandRegister().getEffectsByGroup(effectID).stream()))
			.anyMatch(effectID -> cc.isPlayerEffectActive(effectID, uuid));
	}

	default boolean isArrayActive(@NotNull CCPlayer player) {
		return isActive(player, getEffectArray());
	}

	default List<String> getEffectGroups() {
		return Collections.emptyList();
	}

	default String[] getEffectArray() {
		List<String> list = new ArrayList<>(getEffectGroups());
		list.add(getEffectName().toLowerCase(Locale.US));
		return list.toArray(new String[0]);
	}
}
