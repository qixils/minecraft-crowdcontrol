package dev.qixils.crowdcontrol.common.util.sound;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

class FallbackSound extends AbstractCollectionSound {
	FallbackSound(Sound.@NotNull Source source, float volume, float pitch, Key @NotNull ... sounds) {
		super(source, volume, pitch, sounds);
	}

	@Override
	public @NotNull Optional<Sound> get(@Nullable Predicate<@NotNull Key> validator, Object... args) {
		if (validator == null)
			return Optional.of(sounds.get(0));
		for (Sound sound : sounds) {
			if (validator.test(sound.name())) {
				return Optional.of(sound);
			}
		}
		return Optional.empty();
	}
}
