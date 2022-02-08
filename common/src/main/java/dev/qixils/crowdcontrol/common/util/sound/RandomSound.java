package dev.qixils.crowdcontrol.common.util.sound;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

class RandomSound extends AbstractCollectionSound {
	RandomSound(@NotNull Source source, float volume, float pitch, Key @NotNull ... sounds) {
		super(source, volume, pitch, sounds);
	}

	@Override
	public @NotNull Optional<Sound> get(@Nullable Predicate<@NotNull Key> validator, Object... args) {
		if (validator == null)
			return Optional.of(RandomUtil.randomElementFrom(sounds));
		else
			return RandomUtil.randomElementFrom(sounds, sound -> validator.test(sound.name()));
	}
}
