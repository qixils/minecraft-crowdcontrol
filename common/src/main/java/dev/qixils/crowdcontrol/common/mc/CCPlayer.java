package dev.qixils.crowdcontrol.common.mc;

/**
 * A player in the game.
 */
public interface CCPlayer extends CCLivingEntity {

	/**
	 * Gets the entity's current food level.
	 *
	 * @return the entity's current food level
	 */
	int foodLevel();

	/**
	 * Gets the entity's current saturation level.
	 *
	 * @return the entity's current saturation level
	 */
	double saturation();

	/**
	 * Sets the entity's current food level.
	 *
	 * @param foodLevel the new food level
	 */
	void foodLevel(int foodLevel);

	/**
	 * Sets the entity's current saturation level.
	 *
	 * @param saturation the new saturation level
	 */
	void saturation(double saturation);
}
