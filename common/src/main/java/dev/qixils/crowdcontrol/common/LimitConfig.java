package dev.qixils.crowdcontrol.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static dev.qixils.crowdcontrol.exceptions.ExceptionUtil.validateNotNullElseGet;

/**
 * The configuration for effect limits.
 */
public final class LimitConfig {
	private final boolean hostsBypass;
	private final @NotNull Map<String, Integer> itemLimits;
	private final int itemDefaultLimit;
	private final @NotNull Map<String, Integer> entityLimits;
	private final int entityDefaultLimit;

	/**
	 * Constructs a new limit configuration.
	 *
	 * @param hostsBypass  whether hosts bypass the limits
	 * @param itemLimits   the limits on item effects
	 * @param entityLimits the limits on entity effects
	 */
	public LimitConfig(boolean hostsBypass,
					   @Nullable Map<String, Integer> itemLimits,
					   @Nullable Map<String, Integer> entityLimits) {
		this.hostsBypass = hostsBypass;
		this.itemLimits = new HashMap<>(validateNotNullElseGet(itemLimits, Collections::emptyMap));
		itemDefaultLimit = this.itemLimits.getOrDefault("default", 0);
		this.entityLimits = new HashMap<>(validateNotNullElseGet(entityLimits, Collections::emptyMap));
		entityDefaultLimit = this.entityLimits.getOrDefault("default", 0);
	}

	/**
	 * Constructs an empty limit configuration.
	 */
	public LimitConfig() {
		// TODO: better defaults
		this(true, null, null);
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
		return itemLimits.getOrDefault(item, itemDefaultLimit);
	}

	/**
	 * Gets the limit on the given entity effect.
	 *
	 * @param entity the ID of the entity
	 * @return the limit on the given entity effect
	 */
	public int getEntityLimit(@NotNull String entity) {
		return entityLimits.getOrDefault(entity, entityDefaultLimit);
	}

	@Override
	public String toString() {
		return "LimitConfig{" +
				"hostsBypass=" + hostsBypass +
				", itemLimits=" + itemLimits +
				", itemDefaultLimit=" + itemDefaultLimit +
				", entityLimits=" + entityLimits +
				", entityDefaultLimit=" + entityDefaultLimit +
				'}';
	}
}
