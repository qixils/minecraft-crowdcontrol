package dev.qixils.crowdcontrol.common.util.sound;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;

import static net.kyori.adventure.key.Key.MINECRAFT_NAMESPACE;

/**
 * A collection of sounds ranging from {@link Sound static sounds} to
 * {@link DynamicSound dynamic sounds}.
 */
public final class Sounds {
	private Sounds() {
		throw new IllegalStateException("Utility class cannot be instantiated");
	}

	/**
	 * The message to play to players when Keep Inventory has been enabled for them.
	 */
	public static final Sound KEEP_INVENTORY_ALERT = Sound.sound(
			Key.key(Key.MINECRAFT_NAMESPACE, "block.beacon.activate"),
			Source.MASTER,
			1f,
			1f
	);

	/**
	 * The message to play to players when Keep Inventory has been disabled for them.
	 */
	public static final Sound LOSE_INVENTORY_ALERT = Sound.sound(
			Key.key(Key.MINECRAFT_NAMESPACE, "block.beacon.deactivate"),
			Source.MASTER,
			1f,
			1f
	);

	/**
	 * Collection of randomly selected spooky sounds that can be played by the Spooky Sound command.
	 */
	public static final DynamicSound SPOOKY_SOUND = new RandomSound(
			Source.MASTER,
			1.75f,
			1f,
			Key.key(MINECRAFT_NAMESPACE, "entity.creeper.primed"),
			Key.key(MINECRAFT_NAMESPACE, "entity.enderman.stare"),
			Key.key(MINECRAFT_NAMESPACE, "entity.enderman.scream"),
			Key.key(MINECRAFT_NAMESPACE, "entity.ender_dragon.growl"),
			Key.key(MINECRAFT_NAMESPACE, "entity.ghast.hurt"),
			Key.key(MINECRAFT_NAMESPACE, "entity.generic.explode"),
			Key.key(MINECRAFT_NAMESPACE, "ambient.cave")
	);

	// TODO move more sounds here
}
