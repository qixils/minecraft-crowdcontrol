package dev.qixils.crowdcontrol.common.util;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.Translatable;

@Getter
public class TextUtil {
	private final ComponentFlattener flattener;
	private final PlainTextComponentSerializer serializer;

	public TextUtil(ComponentFlattener flattener) {
		this.flattener = flattener;
		serializer = PlainTextComponentSerializer.builder()
				.flattener(flattener)
				.build();
	}

	public String asPlain(ComponentLike component) {
		return serializer.serialize(component.asComponent());
	}

	/**
	 * Gets the English (en_US) translation for an object.
	 *
	 * @param translatable object to translate
	 * @return translated string
	 */
	public String translate(Translatable translatable) {
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
