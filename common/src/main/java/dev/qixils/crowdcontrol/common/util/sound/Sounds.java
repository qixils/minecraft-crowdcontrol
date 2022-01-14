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
	 * The sound to play to players when Keep Inventory has been enabled for them.
	 */
	public static final DynamicSound KEEP_INVENTORY_ALERT = new FallbackSound(
			Source.MASTER,
			1f,
			1f,
			Key.key(Key.MINECRAFT_NAMESPACE, "block.beacon.activate"),
			Key.key(Key.MINECRAFT_NAMESPACE, "block.cloth.place")
	);

	/**
	 * The sound to play to players when Keep Inventory has been disabled for them.
	 */
	public static final DynamicSound LOSE_INVENTORY_ALERT = new FallbackSound(
			Source.MASTER,
			1f,
			1f,
			Key.key(Key.MINECRAFT_NAMESPACE, "block.beacon.deactivate"),
			Key.key(Key.MINECRAFT_NAMESPACE, "block.bloth.break")
	);

	/**
	 * The sound to play to players upon usage of the Pop-up Command.
	 */
	public static final DynamicSound ANNOYING = new FallbackSound(
			Source.MASTER,
			1,
			1,
			Key.key(MINECRAFT_NAMESPACE, "ui.toast.challenge_complete")
	);

	/**
	 * The sound to play when a lootbox is opened.
	 */
	public static final DynamicSound LOOTBOX_CHIME = new FallbackSound(
			Source.PLAYER,
			1f,
			1.2f,
			Key.key(MINECRAFT_NAMESPACE, "block.note_block.chime"),
			Key.key(MINECRAFT_NAMESPACE, "block.note.chime") // 1.12.2
	);

	/**
	 * The sound to play upon completion of a Do-or-Die task.
	 */
	public static final DynamicSound DO_OR_DIE_SUCCESS_CHIME = new FallbackSound(
			Source.MASTER,
			1f,
			1.5f,
			Key.key(MINECRAFT_NAMESPACE, "block.note_block.chime"),
			Key.key(MINECRAFT_NAMESPACE, "block.note.chime") // 1.12.2
	);

	/**
	 * The sound to play every time the countdown is updated during a Do-or-Die task.
	 */
	public static final DynamicSound DO_OR_DIE_TICK = new FallbackSound(
			Source.MASTER,
			1f,
			1f,
			Key.key(MINECRAFT_NAMESPACE, "block.note_block.bass"),
			Key.key(MINECRAFT_NAMESPACE, "block.note.bass") // 1.12.2
	);

	/**
	 * The sound to play upon spawning of a charged creeper.
	 */
	public static final DynamicSound LIGHTNING_STRIKE = new FallbackSound(
			Source.HOSTILE,
			1f,
			1f,
			Key.key(MINECRAFT_NAMESPACE, "entity.lightning_bolt.thunder"),
			Key.key(MINECRAFT_NAMESPACE, "entity.lightning.thunder") // 1.12.2
	);

	/**
	 * The sound to play when teleporting a player using the teleport command.
	 */
	public static final DynamicSound TELEPORT = new FallbackSound(
			Source.AMBIENT,
			1f,
			1f,
			Key.key(MINECRAFT_NAMESPACE, "entity.enderman.teleport"),
			Key.key(MINECRAFT_NAMESPACE, "entity.endermen.teleport") // 1.12.2
	);

	/**
	 * Collection of randomly selected spooky sounds that can be played by the Spooky Sound command.
	 */
	public static final DynamicSound SPOOKY = new RandomSound(
			Source.MASTER,
			1.75f,
			1f,
			Key.key(MINECRAFT_NAMESPACE, "entity.creeper.primed"),
			Key.key(MINECRAFT_NAMESPACE, "entity.enderman.stare"),
			Key.key(MINECRAFT_NAMESPACE, "entity.enderman.scream"),
			Key.key(MINECRAFT_NAMESPACE, "entity.endermen.stare"), // 1.12.2
			Key.key(MINECRAFT_NAMESPACE, "entity.endermen.scream"), // 1.12.2
			Key.key(MINECRAFT_NAMESPACE, "entity.ender_dragon.growl"),
			Key.key(MINECRAFT_NAMESPACE, "entity.ghast.hurt"),
			Key.key(MINECRAFT_NAMESPACE, "entity.generic.explode"),
			Key.key(MINECRAFT_NAMESPACE, "ambient.cave")
	);
}
