package dev.qixils.crowdcontrol.common.util.sound;

import dev.qixils.crowdcontrol.common.CommandConstants;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * A sound whose returned value is not necessarily consistent.
 * This may be due to various reasons such as randomization or fallbacks for unsupported platforms.
 */
public interface DynamicSound {

	/**
	 * Gets the current {@link Sound} represented by this object.
	 * This may not return the same object every time.
	 * <p>
	 * The default validator set by the platform will be used to ensure that incompatible sounds
	 * (i.e. sounds that are not present in an older version) are not returned.
	 * If no valid sound can be found, an {@link IllegalArgumentException} will be thrown.
	 *
	 * @param args arguments that may or may not be used by the implementing class
	 * @return current {@link Sound}
	 */
	@NotNull
	default Sound get(Object... args) {
		return get(CommandConstants.SOUND_VALIDATOR, args)
				.orElseThrow(() -> new IllegalStateException("No sound could be found"));
	}

	/**
	 * Gets the current {@link Sound} represented by this object.
	 * This may not return the same object every time.
	 * <p>
	 * The {@code validator} will be used to ensure that incompatible sounds (i.e. sounds that are
	 * not present in an older version) are not returned.
	 * A null {@code validator} implies that all sounds are compatible.
	 *
	 * @param validator method that returns {@code true} if the given sound is supported by the
	 *                  current platform
	 * @param args      arguments that may or may not be used by the implementing class
	 * @return an {@link Optional} containing a supported {@link Sound} if one is found, else an
	 * {@link Optional#empty() empty optional}
	 */
	@NotNull
	Optional<Sound> get(@Nullable Predicate<@NotNull Key> validator, Object... args);

	/**
	 * Gets the current {@link Sound} represented by this object.
	 * This may not return the same object every time.
	 * <p>
	 * The {@code validator} will be used to ensure that incompatible sounds (i.e. sounds that are
	 * not present in an older version) are not returned.
	 * A null {@code validator} implies that all sounds are compatible.
	 * If no valid sound can be found, an {@link IllegalArgumentException} will be thrown.
	 *
	 * @param validator method that returns {@code true} if the given sound is supported by the
	 *                  current platform
	 * @return a supported {@link Sound} if one is found
	 * @throws IllegalArgumentException if no valid sound is found
	 */
	@NotNull
	default Sound getOrThrow(@Nullable Predicate<@NotNull Key> validator, Object... args) throws IllegalArgumentException {
		return get(validator, args).orElseThrow(() ->
				new IllegalArgumentException("No supported sound could be found for this platform"));
	}

}
