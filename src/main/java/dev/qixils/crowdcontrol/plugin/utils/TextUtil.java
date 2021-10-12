package dev.qixils.crowdcontrol.plugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.Bukkit;

public class TextUtil {
	public static String asPlain(ComponentLike component) {
		return Bukkit.getUnsafe().plainComponentSerializer().serialize(component.asComponent());
	}

	public static String translate(Translatable translatable) {
		return asPlain(Component.translatable(translatable));
	}

	// borrowed from https://www.baeldung.com/java-string-title-case
	public static String titleCase(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}

		text = text.replace('_', ' ');
		StringBuilder converted = new StringBuilder();

		boolean convertNext = true;
		for (char ch : text.toCharArray()) {
			if (Character.isSpaceChar(ch)) {
				convertNext = true;
			} else if (convertNext) {
				ch = Character.toTitleCase(ch);
				convertNext = false;
			} else {
				ch = Character.toLowerCase(ch);
			}
			converted.append(ch);
		}

		return converted.toString();
	}

	public static String titleCase(Enum<?> enumm) {
		return titleCase(enumm.name());
	}
}
