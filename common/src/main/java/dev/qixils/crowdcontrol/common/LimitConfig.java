package dev.qixils.crowdcontrol.common;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * The configuration for effect limits.
 */
public final class LimitConfig {
	private final boolean hostsBypass;
	private final @NotNull Map<String, Integer> itemLimits;
	private final @NotNull Map<String, Integer> entityLimits;

	/**
	 * Constructs a new limit configuration.
	 *
	 * @param hostsBypass  whether hosts bypass the limits
	 * @param itemLimits   the limits on item effects
	 * @param entityLimits the limits on entity effects
	 */
	public LimitConfig(boolean hostsBypass,
					   @NotNull Map<String, Integer> itemLimits,
					   @NotNull Map<String, Integer> entityLimits) {
		this.hostsBypass = hostsBypass;
		this.itemLimits = new HashMap<>(itemLimits);
		this.entityLimits = new HashMap<>(entityLimits);
	}

	/**
	 * Whether hosts bypass effect limits.
	 *
	 * @return whether hosts bypass effect limits
	 */
	public boolean hostsBypass() {
		return hostsBypass;
	}

	/**
	 * Gets the limit on the given item effect.
	 *
	 * @param item the ID of the item
	 * @return the limit on the given item effect
	 */
	public int getItemLimit(@NotNull String item) {
		return itemLimits.getOrDefault(item, -1);
	}

	/**
	 * Gets the limit on the given entity effect.
	 *
	 * @param entity the ID of the entity
	 * @return the limit on the given entity effect
	 */
	public int getEntityLimit(@NotNull String entity) {
		return entityLimits.getOrDefault(entity, -1);
	}
}
