package dev.qixils.crowdcontrol.plugin.sponge8.utils;

import dev.qixils.crowdcontrol.common.util.TextUtilImpl;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.key.Key.MINECRAFT_NAMESPACE;

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
	public static String csIdOf(Key type) {
		String value = type.value();
		if (!type.namespace().equals(MINECRAFT_NAMESPACE))
			return value;

		switch (value) {
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

	/**
	 * Returns the ID of an object according to the C# file.
	 *
	 * @param type typed object
	 * @return its CS file ID
	 */
	@NotNull
	public static String csIdOf(Keyed type) {
		return csIdOf(type.key());
	}
}
