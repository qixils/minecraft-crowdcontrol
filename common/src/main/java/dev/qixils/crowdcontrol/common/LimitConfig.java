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
	private boolean hostsBypass;
	private @NotNull Map<String, Integer> itemLimits;
	private int itemDefaultLimit;
	private @NotNull Map<String, Integer> entityLimits;
	private int entityDefaultLimit;

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
		itemLimits(validateNotNullElseGet(itemLimits, Collections::emptyMap));
		entityLimits(validateNotNullElseGet(entityLimits, Collections::emptyMap));
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

	public void hostsBypass(boolean hostsBypass) {
		this.hostsBypass = hostsBypass;
	}

	public int defaultItemLimit() {
		return itemDefaultLimit;
	}

	public void defaultItemLimit(int defaultItemLimit) {
		itemLimits.put("default", defaultItemLimit);
		this.itemDefaultLimit = defaultItemLimit;
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
	 * Gets the view of item limits.
	 *
	 * @return the view of item limits
	 */
	public @NotNull Map<String, Integer> itemLimits() {
		return Collections.unmodifiableMap(itemLimits);
	}

	public void itemLimits(Map<String, Integer> itemLimits) {
		this.itemLimits = new HashMap<>(itemLimits);
		itemDefaultLimit = this.itemLimits.getOrDefault("default", 0);
	}

	public int defaultEntityLimit() {
		return entityDefaultLimit;
	}

	public void defaultEntityLimit(int defaultEntityLimit) {
		entityLimits.put("default", defaultEntityLimit);
		this.entityDefaultLimit = defaultEntityLimit;
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

	/**
	 * Gets the view of entity limits.
	 *
	 * @return the view of entity limits
	 */
	public @NotNull Map<String, Integer> entityLimits() {
		return Collections.unmodifiableMap(entityLimits);
	}

	public void entityLimits(Map<String, Integer> entityLimits) {
		this.entityLimits = new HashMap<>(entityLimits);
		entityDefaultLimit = this.entityLimits.getOrDefault("default", 0);
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
