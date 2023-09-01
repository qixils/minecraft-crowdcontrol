package dev.qixils.crowdcontrol.plugin.sponge8.utils;

import dev.qixils.crowdcontrol.common.util.TextUtilImpl;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public class SpongeTextUtil extends TextUtilImpl {

	/**
	 * Creates a new {@link SpongeTextUtil}.
	 */
	public SpongeTextUtil() {
		super(null);
	}

	/**
	 * Returns the ID of an object according to the C# file.
	 *
	 * @param type typed object
	 * @return its CS file ID
	 */
	@NotNull
	public static String csIdOf(Keyed type) {
		Key key = type.key();
		String value = key.value();
//		if (!key.namespace().equals(MINECRAFT_NAMESPACE))
//			return key.value();

		switch (value) {
			case "lightning_bolt":
				return "lightning";
			case "chest_minecart":
				return "minecart_chest";
			case "mooshroom":
				return "mushroom_cow";
			case "tnt":
				return "primed_tnt";
			case "snow_golem":
				return "snowman";
			case "binding_curse":
				return "curse_of_binding";
			case "vanishing_curse":
				return "curse_of_vanishing";
			case "sweeping":
				return "sweeping_edge";
			default:
				return value;
		}
	}
}
