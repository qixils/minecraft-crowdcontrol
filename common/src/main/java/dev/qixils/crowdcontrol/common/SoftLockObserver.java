package dev.qixils.crowdcontrol.common;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.time.LocalDateTime;
import java.util.*;

import static dev.qixils.crowdcontrol.common.Plugin.output;

/**
 * Detects and attempts to resolve players getting soft-locked (i.e. stuck in a death loop).
 *
 * @param <P> The implementation's player type.
 */
@ParametersAreNonnullByDefault
public abstract class SoftLockObserver<P> {
	protected static final Component ALERT = output(Component.translatable("cc.soft-lock.output"));

	private final Map<UUID, DeathData> deathData = new HashMap<>();
	/**
	 * The plugin instance which provides player UUIDs.
	 */
	protected final Plugin<P, ?> plugin;

	/**
	 * Initializes the observer.
	 *
	 * @param plugin The plugin instance which provides player UUIDs.
	 */
	protected SoftLockObserver(Plugin<P, ?> plugin) {
		this.plugin = plugin;
	}

	protected int getSearchH() {
		return plugin.getSoftLockConfig().getSearchH();
	}

	protected int getSearchV() {
		return plugin.getSoftLockConfig().getSearchV();
	}

	/**
	 * Called when a player seems to be soft-locked.
	 *
	 * @param player The soft-locked player.
	 */
	public abstract void onSoftLock(P player);

	/**
	 * Should be called upon a player's death. Used to detect soft-locks.
	 *
	 * @param player The player who died.
	 */
	protected void onDeath(P player) {
		UUID uuid = plugin.playerMapper().tryGetUniqueId(player)
				.orElseThrow(() -> new IllegalArgumentException("Expected player to have a UUID"));
		DeathData data = deathData.computeIfAbsent(uuid, $ -> new DeathData(plugin.getSoftLockConfig()));
		if (data.isSoftLocked()) {
			onSoftLock(player);
			deathData.remove(uuid);
		}
	}

	private static final class DeathData {
		private final List<LocalDateTime> deaths = new ArrayList<>();
		private final @NotNull SoftLockConfig config;

		public DeathData(@NotNull SoftLockConfig config) {
			this.config = config;
			addDeath();
		}

		private void addDeath() {
			LocalDateTime now = LocalDateTime.now();
			deaths.removeIf(d -> d.isBefore(now.minus(config.getPeriod())));
			deaths.add(now);
		}

		public boolean isSoftLocked() {
			addDeath();
			return deaths.size() >= config.getDeaths();
		}
	}
}
