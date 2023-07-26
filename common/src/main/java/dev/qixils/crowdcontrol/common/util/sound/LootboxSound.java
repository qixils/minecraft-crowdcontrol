package dev.qixils.crowdcontrol.common.util.sound;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

import static net.kyori.adventure.key.Key.MINECRAFT_NAMESPACE;

final class LootboxSound implements DynamicSound {
	private static final Logger logger = LoggerFactory.getLogger("CrowdControl/LootboxSound");
	// sounds
	private static final Source SOURCE = Source.PLAYER;
	private static final DynamicSound STANDARD = new FallbackSound(
			SOURCE, 1f, 1.2f,
			Key.key(MINECRAFT_NAMESPACE, "block.note_block.chime"),
			Key.key(MINECRAFT_NAMESPACE, "block.note.chime") // 1.12.2
	);
	private static final DynamicSound LUCKY = new FallbackSound(
			SOURCE, 1f, 1.4f,
			Key.key(MINECRAFT_NAMESPACE, "block.note_block.chime"),
			Key.key(MINECRAFT_NAMESPACE, "block.note.chime") // 1.12.2
	);
	private static final DynamicSound EXTRA_LUCKY = new FallbackSound(
			SOURCE, 0.3f, 1f,
			Key.key(MINECRAFT_NAMESPACE, "ui.toast.challenge_complete")
	);

	// fetch appropriate sound
	@Override
	public @NotNull Optional<Sound> get(@Nullable Predicate<@NotNull Key> validator, Object... args) {
		final int luck;
		if (args == null || args.length == 0)
			luck = 0;
		else if (args[0] instanceof Integer)
			luck = (int) args[0];
		else {
			logger.warn("An invalid argument array was passed: " + Arrays.toString(args));
			luck = 0;
		}

		if (luck >= 10)
			return EXTRA_LUCKY.get(validator);
		else if (luck >= 5)
			return LUCKY.get(validator);
		else
			return STANDARD.get(validator);
	}
}
