package dev.qixils.crowdcontrol.common.mc;

/**
 * A player in the game.
 */
public interface MCCCPlayer extends CCLivingEntity {

	/**
	 * Gets the entity's current food level.
	 *
	 * @return the entity's current food level
	 */
	int foodLevel();

	/**
	 * Sets the entity's current food level.
	 *
	 * @param foodLevel the new food level
	 */
	void foodLevel(int foodLevel);

	/**
	 * Gets the entity's current saturation level.
	 *
	 * @return the entity's current saturation level
	 */
	double saturation();

	/**
	 * Sets the entity's current saturation level.
	 *
	 * @param saturation the new saturation level
	 */
	void saturation(double saturation);

	/**
	 * Gets the entity's current experience level.
	 *
	 * @return the entity's current experience level
	 */
	int xpLevel();

	/**
	 * Sets the entity's current experience level.
	 *
	 * @param xpLevel the new experience level
	 */
	void xpLevel(int xpLevel);

	/**
	 * Adds the given amount of experience levels to the entity.
	 *
	 * @param xpLevel the amount of experience levels to add
	 */
	default void addXpLevel(int xpLevel) {
		xpLevel(Math.max(0, xpLevel() + xpLevel));
	}
}
