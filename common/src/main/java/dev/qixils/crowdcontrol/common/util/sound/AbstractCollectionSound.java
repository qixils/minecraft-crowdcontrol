package dev.qixils.crowdcontrol.common.util.sound;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An abstraction layer for {@link DynamicSound} subclasses that utilize an internal list of sounds.
 */
abstract class AbstractCollectionSound implements DynamicSound {
	protected final List<Sound> sounds;

	protected AbstractCollectionSound(Sound.@NotNull Source source, float volume, float pitch, Key @NotNull ... sounds) {
		if (sounds.length == 0)
			throw new IllegalArgumentException("sounds array cannot be empty");
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

	protected AbstractCollectionSound(Sound @NotNull ... sounds) {
		if (sounds.length == 0)
			throw new IllegalArgumentException("sounds array cannot be empty");
		// abysmal chain of constructors to create an unmodifiable list that is not backed by a
		// mutable array
		this.sounds = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(sounds)));
	}
}
