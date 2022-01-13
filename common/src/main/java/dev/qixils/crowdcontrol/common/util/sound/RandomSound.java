package dev.qixils.crowdcontrol.common.util.sound;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

class RandomSound implements DynamicSound {
	private final List<Sound> sounds;

	RandomSound(@NotNull Source source, float volume, float pitch, Key @NotNull ... sounds) {
		List<Sound> soundList = new ArrayList<>(sounds.length);
		for (Key sound : sounds) {
			soundList.add(Sound.sound(
					sound,
					source,
					volume,
					pitch
			));
		}
		this.sounds = Collections.unmodifiableList(soundList);
	}

	@Override
	public @NotNull Optional<Sound> get(@Nullable Predicate<@NotNull Key> validator) {
		if (validator == null)
			return Optional.of(RandomUtil.randomElementFrom(sounds));
		else
			return RandomUtil.randomElementFrom(sounds, sound -> validator.test(sound.name()));
	}
}
