package dev.qixils.crowdcontrol.common.mc;

import java.util.UUID;

/**
 * A living entity in the game.
 */
public interface CCLivingEntity extends CCEntity {

	UUID MAX_HEALTH_MODIFIER_UUID = new UUID(-899185282624176127L, -7747914881652381318L);
	String MAX_HEALTH_MODIFIER_NAME = "max-health-cc";

	/**
	 * Returns the entity's health.
	 *
	 * @return the entity's health
	 */
	double health();

	/**
	 * Sets the entity's health.
	 *
	 * @param health the new health
	 */
	void health(double health);

	/**
	 * Returns the entity's maximum health.
	 *
	 * @return the entity's maximum health
	 */
	double maxHealth();

	/**
	 * Gets the plugin's offset for the entity's maximum health.
	 *
	 * @return the entity's maximum health offset
	 */
	double maxHealthOffset();

	/**
	 * Sets the plugin's offset for the entity's maximum health.
	 * The provided value should be within the range (-20, {@link Double#MAX_VALUE}].
	 *
	 * @param newOffset the new maximum health offset
	 */
	void maxHealthOffset(double newOffset);

	/**
	 * Deals damage to the entity.
	 *
	 * @param damage the damage to deal
	 */
	void damage(double damage);

	/**
	 * Heals the entity.
	 * 
	 * @param amount the amount to heal
	 */
	void heal(double amount);
}
