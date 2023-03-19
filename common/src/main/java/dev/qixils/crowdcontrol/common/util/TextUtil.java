package dev.qixils.crowdcontrol.common.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.Translatable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for working with Kyori Adventure objects.
 */
public interface TextUtil {

	/**
	 * Gets the {@link ComponentFlattener} instance.
	 * May be null if unsupported by the platform or if the server has not yet been initialized.
	 *
	 * @return flattener
	 */
	@Nullable ComponentFlattener flattener();

	/**
	 * Gets the {@link PlainTextComponentSerializer} instance.
	 *
	 * @return serializer
	 */
	@NotNull PlainTextComponentSerializer serializer();

	/**
	 * Converts a component to plain text.
	 *
	 * @param component component to convert
	 * @return converted text
	 */
	@NotNull
	default String asPlain(@NotNull ComponentLike component) {
		return serializer().serialize(component.asComponent());
	}

	/**
	 * Gets the English (en_US) translation for an object.
	 *
	 * @param translatable object to translate
	 * @return translated string
	 */
	@NotNull
	default String translate(@NotNull Translatable translatable) {
		return asPlain(Component.translatable(translatable));
	}

	/**
	 * Converts a string to Title Case.
	 * Underscores will be replaced with spaces.
	 *
	 * @param text text to convert
	 * @return converted text
	 */
	@Contract("null -> null; !null -> !null")
	static String titleCase(@Nullable String text) {
		// borrowed from https://www.baeldung.com/java-string-title-case
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

	/**
	 * Converts the name of an enum to Title Case.
	 * Underscores will be replaced with spaces.
	 *
	 * @param enm enum value
	 * @return converted text
	 */
	@Contract("null -> null; !null -> !null")
	static String titleCase(@Nullable Enum<?> enm) {
		if (enm == null) return null;
		return titleCase(enm.name());
	}

	/**
	 * Creates a translation key.
	 *
	 * @param prefix prefix
	 * @param key key
	 * @return translation key
	 */
	@NotNull
	static String translationKey(@NotNull String prefix, @NotNull Key key) {
		return prefix + '.' + key.namespace() + '.' + key.value();
	}
}
